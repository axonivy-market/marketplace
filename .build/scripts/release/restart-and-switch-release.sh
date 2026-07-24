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

cleanup_remote_assets() {
    rm -f "${CREDS_TEMP_FILE}" 2>/dev/null || true
}

trap cleanup_remote_assets EXIT

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

echo "Updating current release symlink to ${NEW_RELEASE_NAME}..."
ln -sfn "${NEW_RELEASE_PATH}" "${CURRENT_LINK}"
REMOTE_EOF