#!/usr/bin/env bash

set -euo pipefail

NODE_IP="${1:-}"
RELEASE_VERSION="${2:-}"
NGINX_PORT="${3:-}"
ENV_SECRET_FILE="${4:-}"

if [[ -z "$NODE_IP" || -z "$RELEASE_VERSION" || -z "$NGINX_PORT" || -z "$ENV_SECRET_FILE" ]]; then
    echo "Usage: $0 <node_ip> <release_version> <nginx_port> <env_secret_file>"
    exit 1
fi

SSH_USER="${SSH_REMOTE_USER:-ec2-user}"
SSH_OPTS=( -o StrictHostKeyChecking=accept-new -o ConnectTimeout=10 -o UserKnownHostsFile=~/.ssh/known_hosts )
if [[ -n "${SSH_PRIVATE_KEY_FILE:-}" ]]; then
    SSH_OPTS+=( -i "${SSH_PRIVATE_KEY_FILE}" )
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="${WORKSPACE_ROOT:-$(cd "${SCRIPT_DIR}/../../../../" && pwd)}"
LOCAL_TEMPLATE_NGINX="${WORKSPACE_ROOT}/marketplace-build/nginx/nginx.conf"
TEMPLATE_ROOT="${WORKSPACE_ROOT}/marketplace-build/templates"
LOCAL_TEMPLATE_ENV="${TEMPLATE_ROOT}/.env"
LOCAL_TEMPLATE_COMPOSE="${TEMPLATE_ROOT}/docker-compose.yml"
LOCAL_TEMPLATE_DOCKERFILE="${TEMPLATE_ROOT}/Dockerfile"
REMOTE_TEMPLATE_DIR="/tmp/marketplace-template-${RELEASE_VERSION}-$$"

echo "Nginx Port: ${NGINX_PORT}"
echo "Release: ${RELEASE_VERSION}"

ssh "${SSH_OPTS[@]}" "${SSH_USER}@${NODE_IP}" "mkdir -p '${REMOTE_TEMPLATE_DIR}'"

scp "${SSH_OPTS[@]}" "${LOCAL_TEMPLATE_ENV}" "${SSH_USER}@${NODE_IP}:${REMOTE_TEMPLATE_DIR}/template.env"
scp "${SSH_OPTS[@]}" "${LOCAL_TEMPLATE_COMPOSE}" "${SSH_USER}@${NODE_IP}:${REMOTE_TEMPLATE_DIR}/template.compose.yml"
scp "${SSH_OPTS[@]}" "${LOCAL_TEMPLATE_DOCKERFILE}" "${SSH_USER}@${NODE_IP}:${REMOTE_TEMPLATE_DIR}/template.Dockerfile"
scp "${SSH_OPTS[@]}" "${LOCAL_TEMPLATE_NGINX}" "${SSH_USER}@${NODE_IP}:${REMOTE_TEMPLATE_DIR}/template.nginx.conf"
scp "${SSH_OPTS[@]}" "${ENV_SECRET_FILE}" "${SSH_USER}@${NODE_IP}:${REMOTE_TEMPLATE_DIR}/secret.env"

ssh "${SSH_OPTS[@]}" "${SSH_USER}@${NODE_IP}" \
    "RELEASE_VERSION='${RELEASE_VERSION}' NGINX_PORT='${NGINX_PORT}' REMOTE_TEMPLATE_DIR='${REMOTE_TEMPLATE_DIR}' bash -se" <<'REMOTE_EOF'
set -euo pipefail

cleanup_templates() {
    rm -rf "${REMOTE_TEMPLATE_DIR}" 2>/dev/null || true
}
trap cleanup_templates EXIT

REMOTE_BASE="/home/axonivy/marketplace"
RELEASES_PATH="${REMOTE_BASE}/releases"
CURRENT_LINK="${RELEASES_PATH}/current"
NEW_RELEASE_NAME="${RELEASE_VERSION}"
NEW_RELEASE_PATH="${RELEASES_PATH}/${NEW_RELEASE_NAME}"
SHARED_ENV_FILE="${REMOTE_BASE}/shared/.env"

mkdir -p "${NEW_RELEASE_PATH}/logs" "${NEW_RELEASE_PATH}/config/nginx" "${NEW_RELEASE_PATH}/publish"

merge_env_files() {
    local current_env="$1"
    local template_env="$2"
    local secret_env="$3"
    local output_env="$4"

    declare -A env_vars

    load_env_file() {
        local source_file="$1"
        [[ -f "${source_file}" ]] || return 0
        while IFS='=' read -r key value; do
            [[ -z "${key}" || "${key}" =~ ^# ]] && continue
            env_vars["${key}"]="${value}"
        done < "${source_file}"
    }

    load_env_file "${current_env}"
    load_env_file "${template_env}"
    load_env_file "${secret_env}"

    env_vars["NGINX_PORT"]="${NGINX_PORT}"
    env_vars["RELEASE_VERSION"]="${RELEASE_VERSION}"

    {
        for key in "${!env_vars[@]}"; do
            echo "${key}=${env_vars[$key]}"
        done | sort
    } > "${output_env}"
    chmod 600 "${output_env}"
}

echo "Merging .env for ${NEW_RELEASE_NAME}..."
CURRENT_ENV_FILE=""
if [[ -L "${CURRENT_LINK}" && -f "${CURRENT_LINK}/publish/.env" ]]; then
    CURRENT_ENV_FILE="${CURRENT_LINK}/publish/.env"
elif [[ -L "${CURRENT_LINK}" && -f "${CURRENT_LINK}/.env" ]]; then
    CURRENT_ENV_FILE="${CURRENT_LINK}/.env"
elif [[ -f "${SHARED_ENV_FILE}" ]]; then
    CURRENT_ENV_FILE="${SHARED_ENV_FILE}"
fi

merge_env_files "${CURRENT_ENV_FILE}" "${REMOTE_TEMPLATE_DIR}/template.env" "${REMOTE_TEMPLATE_DIR}/secret.env" "${NEW_RELEASE_PATH}/publish/.env"

echo "Preparing docker-compose.yml for ${NEW_RELEASE_NAME}..."
NEW_COMPOSE="${NEW_RELEASE_PATH}/publish/docker-compose.yml"

cp "${REMOTE_TEMPLATE_DIR}/template.compose.yml" "${NEW_COMPOSE}"

cp "${REMOTE_TEMPLATE_DIR}/template.Dockerfile" "${NEW_RELEASE_PATH}/publish/Dockerfile"

echo "Preparing nginx.conf for ${NEW_RELEASE_NAME}..."
NEW_NGINX="${NEW_RELEASE_PATH}/config/nginx/nginx.conf"

cp "${REMOTE_TEMPLATE_DIR}/template.nginx.conf" "${NEW_NGINX}"

echo "Config location: ${RELEASES_PATH}/${NEW_RELEASE_NAME}/publish/.env"
REMOTE_EOF