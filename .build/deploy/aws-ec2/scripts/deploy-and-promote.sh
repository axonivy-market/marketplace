#!/usr/bin/env bash
# This script is responsible for deploying the new release to the EC2 instance, performing health checks, and promoting the release if the health checks pass.
set -euo pipefail

NODE_IP="${1:-}"
RELEASE_VERSION="${2:-}"
HEALTH_CHECK_TARGETS_ARG="${3:-}"

if [[ -z "$NODE_IP" || -z "$RELEASE_VERSION" || -z "$HEALTH_CHECK_TARGETS_ARG" ]]; then
    echo "Usage: $0 <node_ip> <release_version> <health_check_targets>"
    exit 1
fi

SSH_USER="${SSH_REMOTE_USER:-ec2-user}"
SSH_OPTS=( -o StrictHostKeyChecking=accept-new -o ConnectTimeout=10 -o UserKnownHostsFile=~/.ssh/known_hosts )
if [[ -n "${SSH_PRIVATE_KEY_FILE:-}" ]]; then
    SSH_OPTS+=( -i "${SSH_PRIVATE_KEY_FILE}" )
fi
HEALTH_CHECK_TIMEOUT=300
HEALTH_CHECK_INTERVAL=10

GHCR_CREDS_FILE="$(mktemp)"
chmod 600 "${GHCR_CREDS_FILE}"
trap "rm -f '${GHCR_CREDS_FILE}'" EXIT
echo "${GHCR_USERNAME}" > "${GHCR_CREDS_FILE}"
echo "${GHCR_TOKEN}" >> "${GHCR_CREDS_FILE}"

echo "Node: ${NODE_IP}"
echo "Release: ${RELEASE_VERSION}"

CREDS_TEMP_FILE="/tmp/ghcr-creds-$(date +%s)"
scp "${SSH_OPTS[@]}" "${GHCR_CREDS_FILE}" "${SSH_USER}@${NODE_IP}:${CREDS_TEMP_FILE}" || { echo "Failed to transfer credentials"; exit 1; }

ssh "${SSH_OPTS[@]}" "${SSH_USER}@${NODE_IP}" \
    "RELEASE_VERSION='${RELEASE_VERSION}' HEALTH_CHECK_TARGETS='${HEALTH_CHECK_TARGETS_ARG}' CREDS_TEMP_FILE='${CREDS_TEMP_FILE}' HEALTH_CHECK_TIMEOUT='${HEALTH_CHECK_TIMEOUT}' HEALTH_CHECK_INTERVAL='${HEALTH_CHECK_INTERVAL}' bash -se" <<'REMOTE_EOF'
cleanup_creds() { rm -f "${CREDS_TEMP_FILE}" 2>/dev/null || true; }
trap cleanup_creds EXIT
set -euo pipefail

REMOTE_BASE="/home/axonivy/marketplace"
RELEASES_PATH="${REMOTE_BASE}/releases"
CURRENT_LINK="${RELEASES_PATH}/current"
LIVE_NGINX_PORT="80"
NEW_RELEASE_NAME="${RELEASE_VERSION}"
NEW_RELEASE_PATH="${RELEASES_PATH}/${NEW_RELEASE_NAME}"
NEW_PUBLISH_PATH="${NEW_RELEASE_PATH}/publish"

sanitize_compose_project_name() {
    local input="$1"
    local value

    value="$(printf '%s' "${input}" | tr '[:upper:]' '[:lower:]')"
    value="$(printf '%s' "${value}" | sed -E 's/[^a-z0-9_-]+/-/g; s/^-+//; s/-+$//; s/-+/-/g')"
    value="${value:0:63}"
    value="$(printf '%s' "${value}" | sed -E 's/-+$//')"
    [[ -n "${value}" ]] || value="release"

    printf '%s' "${value}"
}

compose_project_for_release() {
    local release_name="$1"
    printf '%s-release' "$(sanitize_compose_project_name "${release_name}")"
}

NEW_COMPOSE_PROJECT="$(compose_project_for_release "${NEW_RELEASE_NAME}")"

set_env_var() {
    local env_file="$1"
    local key="$2"
    local value="$3"

    if [[ ! -f "${env_file}" ]]; then
        printf '%s=%s\n' "${key}" "${value}" > "${env_file}"
        return 0
    fi

    if grep -q "^${key}=" "${env_file}"; then
        sed -i "s|^${key}=.*|${key}=${value}|" "${env_file}"
    else
        printf '%s=%s\n' "${key}" "${value}" >> "${env_file}"
    fi
}

if [[ -L "${CURRENT_LINK}" ]]; then
    OLD_RELEASE_PATH="$(readlink -f "${CURRENT_LINK}")"
    OLD_RELEASE_NAME="$(basename "${OLD_RELEASE_PATH}")"
    OLD_COMPOSE_PROJECT="$(compose_project_for_release "${OLD_RELEASE_NAME}")"
    if [[ -f "${OLD_RELEASE_PATH}/publish/docker-compose.yml" ]]; then
        OLD_PUBLISH_PATH="${OLD_RELEASE_PATH}/publish"
    else
        OLD_PUBLISH_PATH="${OLD_RELEASE_PATH}"
    fi
else
    OLD_RELEASE_PATH=""
    OLD_RELEASE_NAME=""
    OLD_COMPOSE_PROJECT=""
    OLD_PUBLISH_PATH=""
fi

echo "--- Step 1: Deploy New Release ---"
echo "Logging into ghcr.io..."
GHCR_USERNAME="$(head -n1 "${CREDS_TEMP_FILE}")"
GHCR_TOKEN="$(tail -n1 "${CREDS_TEMP_FILE}")"
echo "${GHCR_TOKEN}" | docker login -u "${GHCR_USERNAME}" --password-stdin ghcr.io >/dev/null 2>&1 || { echo "Failed to login to ghcr.io"; exit 1; }
unset GHCR_TOKEN

if [[ -n "${OLD_RELEASE_NAME}" && "${OLD_RELEASE_NAME}" != "${NEW_RELEASE_NAME}" && -f "${OLD_PUBLISH_PATH}/docker-compose.yml" ]]; then
    echo "Stopping old release ${OLD_RELEASE_NAME}..."
    docker compose -f "${OLD_PUBLISH_PATH}/docker-compose.yml" -p "${OLD_COMPOSE_PROJECT}" --env-file "${OLD_PUBLISH_PATH}/.env" down || true
fi

echo "Starting ${NEW_RELEASE_NAME}..."
docker compose -f "${NEW_PUBLISH_PATH}/docker-compose.yml" -p "${NEW_COMPOSE_PROJECT}" --env-file "${NEW_PUBLISH_PATH}/.env" up -d --pull always

IFS=',' read -r -a RAW_HEALTH_TARGETS <<< "${HEALTH_CHECK_TARGETS}"
HEALTH_TARGETS_LIST=()
for raw_target in "${RAW_HEALTH_TARGETS[@]}"; do
    target="$(echo "${raw_target}" | xargs)"
    [[ -z "${target}" ]] && continue

    if ! [[ "${target}" =~ ^[0-9]{2,5}/[A-Za-z0-9._-]+$ ]]; then
        echo "ERROR: Invalid health check target: ${target}. Expected {port}/{app-name}"
        exit 1
    fi

    HEALTH_TARGETS_LIST+=("${target}")
done

if [[ "${#HEALTH_TARGETS_LIST[@]}" -eq 0 ]]; then
    echo "ERROR: No valid health check targets provided"
    exit 1
fi

echo "Checking /actuator/health for targets: ${HEALTH_TARGETS_LIST[*]}..."
START_TIME="$(date +%s)"
HEALTH_GOOD=false

while true; do
    ELAPSED="$(( $(date +%s) - START_TIME ))"
    if [[ "${ELAPSED}" -ge "${HEALTH_CHECK_TIMEOUT}" ]]; then
        break
    fi

    ALL_HEALTHY=true
    PENDING_STATUS=()
    for health_target in "${HEALTH_TARGETS_LIST[@]}"; do
        health_port="${health_target%%/*}"
        app_name="${health_target#*/}"

        if [[ "${app_name}" == "ROOT" ]]; then
            health_path="/actuator/health"
        else
            health_path="/${app_name}/actuator/health"
        fi

        HEALTH="$(curl -sf "http://127.0.0.1:${health_port}${health_path}" 2>/dev/null | grep -o '"status"[[:space:]]*:[[:space:]]*"[A-Z]*"' | cut -d'"' -f4 || true)"
        if [[ "${HEALTH}" != "UP" ]]; then
            ALL_HEALTHY=false
            PENDING_STATUS+=("${health_target}:${HEALTH:-unknown}")
        fi
    done

    if [[ "${ALL_HEALTHY}" == "true" ]]; then
        HEALTH_GOOD=true
        echo "Health check passed on all targets: ${HEALTH_TARGETS_LIST[*]}"
        break
    fi

    echo "Health check pending: ${PENDING_STATUS[*]} (${ELAPSED}s/${HEALTH_CHECK_TIMEOUT}s)"
    sleep "${HEALTH_CHECK_INTERVAL}"
done

if [[ "${HEALTH_GOOD}" != "true" ]]; then
    echo "Health check failed for ${NEW_RELEASE_NAME}. Rolling back..."
    docker compose -f "${NEW_PUBLISH_PATH}/docker-compose.yml" -p "${NEW_COMPOSE_PROJECT}" --env-file "${NEW_PUBLISH_PATH}/.env" down || true

    if [[ -n "${OLD_RELEASE_NAME}" && -f "${OLD_PUBLISH_PATH}/docker-compose.yml" ]]; then
        echo "Restarting old release ${OLD_RELEASE_NAME}..."
        docker compose -f "${OLD_PUBLISH_PATH}/docker-compose.yml" -p "${OLD_COMPOSE_PROJECT}" --env-file "${OLD_PUBLISH_PATH}/.env" up -d || true
    fi
    exit 1
fi

echo "Deployment health check passed"

echo "--- Step 2: Promote Release ---"
echo "Switching current symlink to ${NEW_RELEASE_NAME}..."
ln -sfn "${NEW_RELEASE_PATH}" "${CURRENT_LINK}"

echo "Switching promoted release to live nginx port ${LIVE_NGINX_PORT}..."
set_env_var "${NEW_PUBLISH_PATH}/.env" "NGINX_PORT" "${LIVE_NGINX_PORT}"
docker compose -f "${NEW_PUBLISH_PATH}/docker-compose.yml" -p "${NEW_COMPOSE_PROJECT}" --env-file "${NEW_PUBLISH_PATH}/.env" up -d --force-recreate nginx

echo "Reloading nginx..."
if docker ps --format '{{.Names}}' | grep -q 'nginx'; then
    CONTAINER_ID="$(docker ps -qf 'name=nginx' | head -n1)"
    if [[ -n "${CONTAINER_ID}" ]]; then
        docker exec "${CONTAINER_ID}" nginx -s reload || true
        echo "Nginx reloaded"
    fi
elif command -v nginx >/dev/null 2>&1; then
    nginx -s reload || true
    echo "Nginx reloaded"
else
    echo "Nginx not found, skipping reload"
fi

if [[ -n "${OLD_RELEASE_NAME}" && "${OLD_RELEASE_NAME}" != "${NEW_RELEASE_NAME}" && -f "${OLD_PUBLISH_PATH}/docker-compose.yml" ]]; then
    echo "Cleaning up old release ${OLD_RELEASE_NAME}..."
    docker compose -f "${OLD_PUBLISH_PATH}/docker-compose.yml" -p "${OLD_COMPOSE_PROJECT}" --env-file "${OLD_PUBLISH_PATH}/.env" down 2>/dev/null || true
    echo "Old release stopped"
fi

echo "Pruning unused Docker images..."
docker image prune -af --filter "until=24h" || true

echo "Promotion of ${NEW_RELEASE_NAME} complete"
REMOTE_EOF
