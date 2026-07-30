#!/usr/bin/env bash
# Restarts target release containers on the node and updates the current release symlink.
set -euo pipefail

NODE_IP="${1:-}"
RELEASE_VERSION="${2:-}"

if [[ -z "${NODE_IP}" || -z "${RELEASE_VERSION}" ]]; then
    echo "ERROR: $0 Missing required arguments: <NODE_IP> <RELEASE_VERSION>"
    exit 1
fi

SSH_USER="${SSH_REMOTE_USER:-ec2-user}"
SSH_OPTS=( -o StrictHostKeyChecking=accept-new -o ConnectTimeout=10 -o UserKnownHostsFile=~/.ssh/known_hosts )
if [[ -n "${SSH_PRIVATE_KEY_FILE:-}" ]]; then
    SSH_OPTS+=( -i "${SSH_PRIVATE_KEY_FILE}" )
fi

REMOTE_CREDS_FILE="/tmp/marketplace-ghcr-creds-$(date +%s)-$$"

GHCR_CREDS_FILE="$(mktemp)"
chmod 600 "${GHCR_CREDS_FILE}"
trap 'rm -f "${GHCR_CREDS_FILE}"' EXIT
echo "${GHCR_USERNAME}" > "${GHCR_CREDS_FILE}"
echo "${GHCR_TOKEN}" >> "${GHCR_CREDS_FILE}"

echo "Node: ${NODE_IP}"
echo "Release: ${RELEASE_VERSION}"
echo "Restarting release containers from the fixed remote release path"

ssh "${SSH_OPTS[@]}" "${SSH_USER}@${NODE_IP}" \
    "cat > '${REMOTE_CREDS_FILE}' && chmod 600 '${REMOTE_CREDS_FILE}'" < "${GHCR_CREDS_FILE}" || {
    echo "Failed to transfer GHCR credentials"
    exit 1
}

ssh "${SSH_OPTS[@]}" "${SSH_USER}@${NODE_IP}" \
    "RELEASE_VERSION='${RELEASE_VERSION}' CREDS_TEMP_FILE='${REMOTE_CREDS_FILE}' bash -se" <<'REMOTE_EOF'
set -euo pipefail

REMOTE_BASE="/home/axonivy/marketplace"
RELEASES_PATH="${REMOTE_BASE}/releases"
CURRENT_LINK="${RELEASES_PATH}/current"
NEW_RELEASE_NAME="${RELEASE_VERSION}"
NEW_RELEASE_PATH="${RELEASES_PATH}/${NEW_RELEASE_NAME}"
NEW_PUBLISH_PATH="${NEW_RELEASE_PATH}/publish"

# Removes temporary remote GHCR credentials file before script exits.
cleanup_remote_assets() {
    rm -f "${CREDS_TEMP_FILE}" 2>/dev/null || true
}

trap cleanup_remote_assets EXIT

# Normalizes values into compose-safe lowercase dash-separated segments.
sanitize_compose_segment() {
    local value="$1"

    value="$(printf '%s' "${value}" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/-/g; s/^-+//; s/-+$//; s/-+/-/g')"
    printf '%s' "${value}"
}

RELEASE_ENV_RAW="${TARGET_ENV:-${RELEASE_ENV:-prod}}"
RELEASE_ENV="$(sanitize_compose_segment "${RELEASE_ENV_RAW}")"
[[ -n "${RELEASE_ENV}" ]] || RELEASE_ENV="prod"

# Builds standardized compose project names for release resources.
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
    [[ -n "${service_value}" ]] || service_value="app"
    [[ -n "${version_value}" ]] || version_value="latest"

    if [[ "${env_value}" == "prod" ]]; then
        printf 'market-%s-%s' "${service_value}" "${version_value}"
    else
        printf 'market-%s-%s-%s' "${env_value}" "${service_value}" "${version_value}"
    fi
}

# Returns compose project name for app service in a specific release.
compose_project_for_release() {
    local release_name="$1"
    compose_project_name "${RELEASE_ENV}" "app" "${release_name}"
}

# Returns the full container name for a release-scoped service.
container_name_for_release_service() {
    local service_name="$1"
    local release_name="${2:-${NEW_RELEASE_NAME}}"

    compose_project_name "${RELEASE_ENV}" "${service_name}" "${release_name}"
}

# Resolves nginx current symlink path for the active environment.
nginx_current_link_for_env() {
    if [[ "${RELEASE_ENV}" == "prod" ]]; then
        printf '%s/nginx/current' "${REMOTE_BASE}"
    else
        printf '%s/nginx/%s/current' "${REMOTE_BASE}" "${RELEASE_ENV}"
    fi
}

# Resolves active nginx compose project name from current symlink.
current_nginx_project_for_env() {
    local nginx_current_link
    local nginx_release_path
    local nginx_release_name

    nginx_current_link="$(nginx_current_link_for_env)"
    [[ -L "${nginx_current_link}" ]] || return 1

    nginx_release_path="$(readlink -f "${nginx_current_link}" 2>/dev/null || true)"
    [[ -n "${nginx_release_path}" ]] || return 1

    nginx_release_name="$(basename "${nginx_release_path}")"
    compose_project_name "${RELEASE_ENV}" 'nginx' "${nginx_release_name}"
}

# Restarts active nginx compose container for this environment when found.
restart_nginx_for_env() {
    local nginx_project
    local container_id

    if nginx_project="$(current_nginx_project_for_env)"; then
        container_id="$(docker ps -q \
            --filter "label=com.docker.compose.project=${nginx_project}" \
            --filter "label=com.docker.compose.service=nginx" | head -n1 || true)"

        if [[ -n "${container_id}" ]]; then
            echo "Restarting nginx container for project ${nginx_project}..."
            docker restart "${container_id}" >/dev/null
            echo "Nginx restarted"
            return 0
        fi
    fi

    echo "Nginx container for current env not found, skipping restart"
}

NEW_COMPOSE_PROJECT="$(compose_project_for_release "${NEW_RELEASE_NAME}")"
OLD_RELEASE_NAME=""
OLD_PUBLISH_PATH=""
OLD_COMPOSE_PROJECT=""

if [[ -L "${CURRENT_LINK}" ]]; then
    OLD_RELEASE_PATH="$(readlink -f "${CURRENT_LINK}")"
    OLD_RELEASE_NAME="$(basename "${OLD_RELEASE_PATH}")"
    OLD_COMPOSE_PROJECT="$(compose_project_for_release "${OLD_RELEASE_NAME}")"
    if [[ -f "${OLD_RELEASE_PATH}/publish/docker-compose.yml" ]]; then
        OLD_PUBLISH_PATH="${OLD_RELEASE_PATH}/publish"
    else
        OLD_PUBLISH_PATH="${OLD_RELEASE_PATH}"
    fi
fi

if [[ ! -f "${NEW_PUBLISH_PATH}/docker-compose.yml" ]]; then
    echo "ERROR: Missing docker-compose file: ${NEW_PUBLISH_PATH}/docker-compose.yml"
    exit 1
fi

UI_CONTAINER_NAME="$(container_name_for_release_service 'ui')"
APP_CONTAINER_NAME="$(container_name_for_release_service 'app')"
STABLE_CONTAINER_NAME="$(container_name_for_release_service 'stable')"
export UI_CONTAINER_NAME APP_CONTAINER_NAME STABLE_CONTAINER_NAME

echo "Logging into ghcr.io..."
GHCR_USERNAME="$(head -n1 "${CREDS_TEMP_FILE}")"
GHCR_TOKEN="$(tail -n1 "${CREDS_TEMP_FILE}")"
echo "${GHCR_TOKEN}" | docker login -u "${GHCR_USERNAME}" --password-stdin ghcr.io >/dev/null 2>&1 || {
    echo "Failed to login to ghcr.io"
    exit 1
}

if [[ -n "${OLD_RELEASE_NAME}" && "${OLD_RELEASE_NAME}" != "${NEW_RELEASE_NAME}" && -f "${OLD_PUBLISH_PATH}/docker-compose.yml" ]]; then
    echo "Stopping old release ${OLD_RELEASE_NAME}..."
    docker compose -f "${OLD_PUBLISH_PATH}/docker-compose.yml" -p "${OLD_COMPOSE_PROJECT}" --env-file "${OLD_PUBLISH_PATH}/.env" down || true
fi

echo "Starting ${NEW_RELEASE_NAME}..."
docker compose -f "${NEW_PUBLISH_PATH}/docker-compose.yml" -p "${NEW_COMPOSE_PROJECT}" --env-file "${NEW_PUBLISH_PATH}/.env" up -d --pull always

restart_nginx_for_env

echo "Updating current release symlink to ${NEW_RELEASE_NAME}..."
ln -sfn "${NEW_RELEASE_PATH}" "${CURRENT_LINK}"
REMOTE_EOF