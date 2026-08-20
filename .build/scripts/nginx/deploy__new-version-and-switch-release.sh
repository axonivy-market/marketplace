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
EXTERNAL_NETWORK_NAME="${NGINX_EXTERNAL_NETWORK:-marketplace-network}"

# Normalizes values into compose-safe lowercase dash-separated segments.
sanitize_compose_segment() {
    local value="$1"

    value="$(printf '%s' "${value}" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/-/g; s/^-+//; s/-+$//; s/-+/-/g')"
    printf '%s' "${value}"
}

# Builds the standardized compose project name for nginx releases.
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

# Inserts or replaces a key in an env file.
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

# Ensures required docker external network exists before compose up.
ensure_external_network_exists() {
    local network_name="$1"

    if [[ -z "${network_name}" ]]; then
        echo "ERROR: External network name is empty."
        return 1
    fi

    if ! docker network inspect "${network_name}" >/dev/null 2>&1; then
        echo "External network ${network_name} does not exist. Creating it..."
        docker network create "${network_name}" >/dev/null
    fi
}

# Checks whether a container is attached to a specific docker network.
container_connected_to_network() {
    local container_id="$1"
    local network_name="$2"

    docker inspect "${container_id}" --format '{{json .NetworkSettings.Networks}}' 2>/dev/null | grep -q "\"${network_name}\":"
}

# Connects a container to network and validates the attachment.
ensure_container_connected_to_network() {
    local container_id="$1"
    local network_name="$2"

    if ! container_connected_to_network "${container_id}" "${network_name}"; then
        echo "Container ${container_id} is not attached to ${network_name}. Reconnecting..."
        docker network connect "${network_name}" "${container_id}" >/dev/null
    fi

    if ! container_connected_to_network "${container_id}" "${network_name}"; then
        echo "ERROR: Container ${container_id} is still not attached to ${network_name}."
        return 1
    fi
}

OLD_RELEASE_NAME=""
OLD_RELEASE_PATH=""
OLD_COMPOSE_PROJECT=""
OLD_NGINX_DOCKER_COMPOSE_FILE=""
OLD_NGINX_ENV_FILE=""

if [[ -L "${CURRENT_LINK}" ]]; then
    OLD_RELEASE_PATH="$(readlink -f "${CURRENT_LINK}")"
    OLD_RELEASE_NAME="$(basename "${OLD_RELEASE_PATH}")"
    OLD_COMPOSE_PROJECT="$(compose_project_name "${TARGET_ENV}" 'nginx' "${OLD_RELEASE_NAME}")"
    OLD_NGINX_DOCKER_COMPOSE_FILE="${OLD_RELEASE_PATH}/docker-compose.yml"
    OLD_NGINX_ENV_FILE="${OLD_RELEASE_PATH}/.env"
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
upsert_env_value "${NEW_NGINX_ENV_FILE}" "NGINX_EXTERNAL_NETWORK" "${EXTERNAL_NETWORK_NAME}"
upsert_env_value "${NEW_NGINX_ENV_FILE}" "NGINX_CONTAINER_NAME" "${NEW_COMPOSE_PROJECT}"

ensure_external_network_exists "${EXTERNAL_NETWORK_NAME}"

# Best-effort rollback to previous nginx release when cutover fails.
rollback_to_old_release() {
    # Best-effort rollback to the previously active release on startup failure.
    if [[ -n "${OLD_RELEASE_NAME}" && "${OLD_RELEASE_NAME}" != "${NEW_RELEASE_NAME}" ]]; then
        echo "Rolling back to old release ${OLD_RELEASE_NAME}..."
        if [[ -n "${OLD_RELEASE_PATH}" ]]; then
            ln -sfn "${OLD_RELEASE_PATH}" "${CURRENT_LINK}" || true
        fi

        if [[ -f "${OLD_NGINX_DOCKER_COMPOSE_FILE}" ]]; then
            if [[ -f "${OLD_NGINX_ENV_FILE}" ]]; then
                docker compose -f "${OLD_NGINX_DOCKER_COMPOSE_FILE}" -p "${OLD_COMPOSE_PROJECT}" --env-file "${OLD_NGINX_ENV_FILE}" up -d --force-recreate || true
            else
                docker compose -f "${OLD_NGINX_DOCKER_COMPOSE_FILE}" -p "${OLD_COMPOSE_PROJECT}" up -d --force-recreate || true
            fi
        fi
    fi
}

# Waits for temporary green container health before switching live traffic.
wait_for_green_container() {
    local container_id="$1"
    local timeout_seconds="${2:-90}"
    local started_at
    local health_status

    started_at="$(date +%s)"
    while true; do
        if ! docker inspect "${container_id}" >/dev/null 2>&1; then
            echo "ERROR: Green container ${container_id} no longer exists."
            return 1
        fi

        if [[ "$(docker inspect -f '{{.State.Running}}' "${container_id}" 2>/dev/null || echo false)" != "true" ]]; then
            echo "ERROR: Green container ${container_id} is not running."
            docker logs "${container_id}" 2>/dev/null || true
            return 1
        fi

        health_status="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' "${container_id}" 2>/dev/null || echo none)"
        if [[ "${health_status}" == "healthy" ]]; then
            echo "Green container reported healthy by Docker healthcheck."
            return 0
        fi

        if [[ "${health_status}" == "unhealthy" ]]; then
            echo "ERROR: Green container reported unhealthy by Docker healthcheck."
            docker logs "${container_id}" 2>/dev/null || true
            return 1
        fi

        # If no Docker healthcheck is defined, verify nginx answers internally.
        if [[ "${health_status}" == "none" ]]; then
            if docker exec "${container_id}" sh -c 'wget -q -O /dev/null http://127.0.0.1:80 >/dev/null 2>&1 || curl -sS -o /dev/null http://127.0.0.1:80 >/dev/null 2>&1'; then
                echo "Green container responded on internal port 80."
                return 0
            fi
        fi

        if (( $(date +%s) - started_at >= timeout_seconds )); then
            echo "ERROR: Timeout waiting for green container health (${timeout_seconds}s)."
            docker logs "${container_id}" 2>/dev/null || true
            return 1
        fi

        sleep 3
    done
}

NEW_SERVICE_NAME="$(docker compose -f "${NEW_NGINX_DOCKER_COMPOSE_FILE}" -p "${NEW_COMPOSE_PROJECT}" --env-file "${NEW_NGINX_ENV_FILE}" config --services | head -n1 | tr -d '[:space:]')"
if [[ -z "${NEW_SERVICE_NAME}" ]]; then
    echo "ERROR: Could not determine nginx service name from compose file."
    exit 1
fi

GREEN_CONTAINER_NAME="${NEW_COMPOSE_PROJECT}-green"

echo "Building nginx image for release ${NEW_RELEASE_NAME}..."
docker compose -f "${NEW_NGINX_DOCKER_COMPOSE_FILE}" -p "${NEW_COMPOSE_PROJECT}" --env-file "${NEW_NGINX_ENV_FILE}" build

echo "Starting green container ${GREEN_CONTAINER_NAME} without published ports..."
docker rm -f "${GREEN_CONTAINER_NAME}" >/dev/null 2>&1 || true
GREEN_CONTAINER_ID="$(docker compose -f "${NEW_NGINX_DOCKER_COMPOSE_FILE}" -p "${NEW_COMPOSE_PROJECT}" --env-file "${NEW_NGINX_ENV_FILE}" run -d --name "${GREEN_CONTAINER_NAME}" --no-deps "${NEW_SERVICE_NAME}")"

if ! wait_for_green_container "${GREEN_CONTAINER_ID}" 90; then
    echo "Healthcheck failed for green container. Keeping current release untouched."
    docker rm -f "${GREEN_CONTAINER_NAME}" >/dev/null 2>&1 || true
    exit 1
fi

echo "Green healthcheck passed. Removing green test container before cutover..."
docker rm -f "${GREEN_CONTAINER_NAME}" >/dev/null 2>&1 || true

if [[ -n "${OLD_RELEASE_NAME}" && "${OLD_RELEASE_NAME}" != "${NEW_RELEASE_NAME}" && -f "${OLD_NGINX_DOCKER_COMPOSE_FILE}" ]]; then
    echo "Stopping old nginx release ${OLD_RELEASE_NAME}..."
    if [[ -f "${OLD_NGINX_ENV_FILE}" ]]; then
        docker compose -f "${OLD_NGINX_DOCKER_COMPOSE_FILE}" -p "${OLD_COMPOSE_PROJECT}" --env-file "${OLD_NGINX_ENV_FILE}" down || true
    else
        docker compose -f "${OLD_NGINX_DOCKER_COMPOSE_FILE}" -p "${OLD_COMPOSE_PROJECT}" down || true
    fi
fi

echo "Starting nginx for release ${NEW_RELEASE_NAME} with configured external port..."
# --force-recreate guarantees a fresh container so proxy_cache (container-local /tmp/nginx_cache) is never reused from a stale container.
if ! docker compose -f "${NEW_NGINX_DOCKER_COMPOSE_FILE}" -p "${NEW_COMPOSE_PROJECT}" --env-file "${NEW_NGINX_ENV_FILE}" up -d --build --force-recreate; then
    echo "ERROR: Failed to start nginx for release ${NEW_RELEASE_NAME} after cutover."
    rollback_to_old_release
    exit 1
fi

NEW_CONTAINER_ID="$(docker compose -f "${NEW_NGINX_DOCKER_COMPOSE_FILE}" -p "${NEW_COMPOSE_PROJECT}" --env-file "${NEW_NGINX_ENV_FILE}" ps -q "${NEW_SERVICE_NAME}" | head -n1 | tr -d '[:space:]')"
if [[ -z "${NEW_CONTAINER_ID}" ]]; then
    echo "ERROR: Could not determine running nginx container id for release ${NEW_RELEASE_NAME}."
    rollback_to_old_release
    exit 1
fi

if ! ensure_container_connected_to_network "${NEW_CONTAINER_ID}" "${EXTERNAL_NETWORK_NAME}"; then
    rollback_to_old_release
    exit 1
fi

echo "Switching nginx current symlink to ${NEW_RELEASE_NAME}..."
ln -sfn "${NEW_RELEASE_PATH}" "${CURRENT_LINK}"

echo "Nginx green-stop-restart deploy complete for ${NEW_RELEASE_NAME}"
REMOTE_EOF
