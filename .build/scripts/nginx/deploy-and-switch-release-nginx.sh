#!/usr/bin/env bash
# Manages nginx release activation on a node by starting the target stack and switching current.
set -euo pipefail

NODE_IP="${1:-}"
NGINX_VERSION="${2:-}"
TARGET_ENV="${3:-default}"

if [[ -z "${NODE_IP}" || -z "${NGINX_VERSION}" ]]; then
    echo "ERROR: $0 Missing required arguments: <NODE_IP> <NGINX_VERSION> [TARGET_ENV]"
    exit 1
fi

TARGET_ENV="$(printf '%s' "${TARGET_ENV}" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/-/g; s/^-+//; s/-+$//')"
if [[ -z "${TARGET_ENV}" ]]; then
    echo "ERROR: Invalid TARGET_ENV after normalization."
    exit 1
fi

SSH_USER="${SSH_REMOTE_USER:-ec2-user}"
SSH_OPTS=( -o StrictHostKeyChecking=accept-new -o ConnectTimeout=10 -o UserKnownHostsFile=~/.ssh/known_hosts )
if [[ -n "${SSH_PRIVATE_KEY_FILE:-}" ]]; then
    SSH_OPTS+=( -i "${SSH_PRIVATE_KEY_FILE}" )
fi

echo "Node: ${NODE_IP}"
echo "Release: ${NGINX_VERSION}"
echo "Target env: ${TARGET_ENV}"
if [[ "${TARGET_ENV}" == "prod" ]]; then
    echo "Deploying nginx from fixed remote path /home/axonivy/marketplace/nginx/${NGINX_VERSION}"
else
    echo "Deploying nginx from fixed remote path /home/axonivy/marketplace/nginx/${TARGET_ENV}/${NGINX_VERSION}"
fi

ssh "${SSH_OPTS[@]}" "${SSH_USER}@${NODE_IP}" \
    "NGINX_VERSION='${NGINX_VERSION}' TARGET_ENV='${TARGET_ENV}' bash -se" <<'REMOTE_EOF'
set -euo pipefail

if [[ "${TARGET_ENV}" == "prod" ]]; then
    REMOTE_NGINX_BASE="/home/axonivy/marketplace/nginx"
else
    REMOTE_NGINX_BASE="/home/axonivy/marketplace/nginx/${TARGET_ENV}"
fi
CURRENT_LINK="${REMOTE_NGINX_BASE}/current"
NEW_RELEASE_NAME="${NGINX_VERSION}"
NEW_RELEASE_PATH="${REMOTE_NGINX_BASE}/${NEW_RELEASE_NAME}"
NEW_NGINX_CONFIG_FILE="${NEW_RELEASE_PATH}/nginx.conf"
NEW_NGINX_DOCKER_COMPOSE_FILE="${NEW_RELEASE_PATH}/docker-compose.yml"
NEW_NGINX_ENV_FILE="${NEW_RELEASE_PATH}/.env"

sanitize_compose_segment() {
    local value="$1"

    value="$(printf '%s' "${value}" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/-/g; s/^-+//; s/-+$//; s/-+/-/g')"
    printf '%s' "${value}"
}

compose_project_name() {
    local target_env="$1"
    local service_name="$2"
    local version="$3"
    local env_value
    local service_value
    local version_value

    env_value="$(sanitize_compose_segment "${target_env}")"
    service_value="$(sanitize_compose_segment "${service_name}")"
    version_value="$(sanitize_compose_segment "${version}")"

    [[ -n "${env_value}" ]] || env_value="prod"
    [[ -n "${service_value}" ]] || service_value="nginx"
    [[ -n "${version_value}" ]] || version_value="latest"

    if [[ "${env_value}" == "prod" ]]; then
        printf 'market-%s-%s' "${service_value}" "${version_value}"
    else
        printf 'market-%s-%s-%s' "${env_value}" "${service_value}" "${version_value}"
    fi
}

upsert_env_value() {
    local env_file="$1"
    local key="$2"
    local value="$3"

    if grep -qE "^${key}=" "${env_file}"; then
        sed -i -E "s|^${key}=.*$|${key}=${value}|" "${env_file}"
    else
        echo "${key}=${value}" >> "${env_file}"
    fi
}

OLD_RELEASE_NAME=""
OLD_RELEASE_PATH=""
OLD_COMPOSE_PROJECT=""
OLD_NGINX_CONFIG_DIR=""
OLD_NGINX_DOCKER_COMPOSE_FILE=""
OLD_NGINX_ENV_FILE=""

if [[ -L "${CURRENT_LINK}" ]]; then
    OLD_RELEASE_PATH="$(readlink -f "${CURRENT_LINK}")"
    OLD_RELEASE_NAME="$(basename "${OLD_RELEASE_PATH}")"
    OLD_COMPOSE_PROJECT="$(compose_project_name "${TARGET_ENV}" 'nginx' "${OLD_RELEASE_NAME}")"
    OLD_NGINX_CONFIG_DIR="${OLD_RELEASE_PATH}"
    OLD_NGINX_DOCKER_COMPOSE_FILE="${OLD_NGINX_CONFIG_DIR}/docker-compose.yml"
    OLD_NGINX_ENV_FILE="${OLD_NGINX_CONFIG_DIR}/.env"
fi

NEW_COMPOSE_PROJECT="$(compose_project_name "${TARGET_ENV}" 'nginx' "${NEW_RELEASE_NAME}")"

if [[ ! -f "${NEW_NGINX_CONFIG_FILE}" ]]; then
    echo "ERROR: Missing nginx config: ${NEW_NGINX_CONFIG_FILE}"
    exit 1
fi

if [[ ! -f "${NEW_NGINX_DOCKER_COMPOSE_FILE}" ]]; then
    echo "ERROR: Missing docker-compose file: ${NEW_NGINX_DOCKER_COMPOSE_FILE}"
    exit 1
fi

if [[ ! -f "${NEW_NGINX_ENV_FILE}" ]]; then
    touch "${NEW_NGINX_ENV_FILE}"
fi

# Ensure runtime values in .env always point to the fixed nginx release path.
upsert_env_value "${NEW_NGINX_ENV_FILE}" "NGINX_CONFIG_PATH" "${NEW_NGINX_CONFIG_FILE}"
upsert_env_value "${NEW_NGINX_ENV_FILE}" "NGINX_LOG_PATH" "${NEW_RELEASE_PATH}/logs"
upsert_env_value "${NEW_NGINX_ENV_FILE}" "NGINX_CACHE_PATH" "/home/axonivy/marketplace/data/cache"

rollback_to_old_release() {
    # Best-effort rollback to the previously active release on startup failure.
    if [[ -n "${OLD_RELEASE_NAME}" && "${OLD_RELEASE_NAME}" != "${NEW_RELEASE_NAME}" ]]; then
        echo "Rolling back to old release ${OLD_RELEASE_NAME}..."
        if [[ -n "${OLD_RELEASE_PATH}" ]]; then
            ln -sfn "${OLD_RELEASE_PATH}" "${CURRENT_LINK}" || true
        fi

        if [[ -f "${OLD_NGINX_DOCKER_COMPOSE_FILE}" ]]; then
            if [[ -f "${OLD_NGINX_ENV_FILE}" ]]; then
                docker compose -f "${OLD_NGINX_DOCKER_COMPOSE_FILE}" -p "${OLD_COMPOSE_PROJECT}" --env-file "${OLD_NGINX_ENV_FILE}" up -d || true
            else
                docker compose -f "${OLD_NGINX_DOCKER_COMPOSE_FILE}" -p "${OLD_COMPOSE_PROJECT}" up -d || true
            fi
        fi
    fi
}

echo "Starting nginx for release ${NEW_RELEASE_NAME}..."
if ! docker compose -f "${NEW_NGINX_DOCKER_COMPOSE_FILE}" -p "${NEW_COMPOSE_PROJECT}" --env-file "${NEW_NGINX_ENV_FILE}" up -d --build; then
    echo "ERROR: Failed to start nginx for release ${NEW_RELEASE_NAME}."
    rollback_to_old_release
    exit 1
fi

echo "Switching nginx current symlink to ${NEW_RELEASE_NAME}..."
ln -sfn "${NEW_RELEASE_PATH}" "${CURRENT_LINK}"

if [[ -n "${OLD_RELEASE_NAME}" && "${OLD_RELEASE_NAME}" != "${NEW_RELEASE_NAME}" && -f "${OLD_NGINX_DOCKER_COMPOSE_FILE}" ]]; then
    echo "Stopping old nginx release ${OLD_RELEASE_NAME}..."
    if [[ -f "${OLD_NGINX_ENV_FILE}" ]]; then
        docker compose -f "${OLD_NGINX_DOCKER_COMPOSE_FILE}" -p "${OLD_COMPOSE_PROJECT}" --env-file "${OLD_NGINX_ENV_FILE}" down || true
    else
        docker compose -f "${OLD_NGINX_DOCKER_COMPOSE_FILE}" -p "${OLD_COMPOSE_PROJECT}" down || true
    fi
fi

echo "Nginx deploy and switch complete for ${NEW_RELEASE_NAME}"
REMOTE_EOF
