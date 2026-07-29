#!/usr/bin/env bash
# Prepares nginx release assets on the node by uploading config, compose, Dockerfile, and env.
set -euo pipefail

NODE_IP="${1:-}"
NGINX_VERSION="${2:-}"
TARGET_ENV="${3:-default}"
WORKSPACE_ROOT="${4:-}"
NGINX_CONFIG_PATH="${5:-}"
NGINX_PORT="${6:-80}"

missing_args=()
[[ -z "${NODE_IP}" ]] && missing_args+=("NODE_IP")
[[ -z "${NGINX_VERSION}" ]] && missing_args+=("NGINX_VERSION")
[[ -z "${WORKSPACE_ROOT}" ]] && missing_args+=("WORKSPACE_ROOT")
[[ -z "${NGINX_CONFIG_PATH}" ]] && missing_args+=("NGINX_CONFIG_PATH")
if [[ ${#missing_args[@]} -gt 0 ]]; then
    echo "ERROR: $0 Missing required arguments: ${missing_args[*]}"
    echo "Usage: $0 <NODE_IP> <NGINX_VERSION> <TARGET_ENV> <WORKSPACE_ROOT> <NGINX_CONFIG_PATH> [NGINX_PORT]"
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

LOCAL_NGINX_CONFIG="${WORKSPACE_ROOT}/target-source/${NGINX_CONFIG_PATH}"
if [[ ! -f "${LOCAL_NGINX_CONFIG}" ]]; then
    echo "ERROR: Required file not found: ${LOCAL_NGINX_CONFIG}"
    exit 1
fi

LOCAL_FIXED_DOCKER_COMPOSE="${WORKSPACE_ROOT}/target-source/marketplace-build/nginx/docker-compose.yml"
LOCAL_FIXED_DOCKERFILE="${WORKSPACE_ROOT}/target-source/marketplace-build/nginx/Dockerfile"

if [[ ! -f "${LOCAL_FIXED_DOCKER_COMPOSE}" ]]; then
    echo "ERROR: Required file not found: ${LOCAL_FIXED_DOCKER_COMPOSE}"
    exit 1
fi

if [[ ! -f "${LOCAL_FIXED_DOCKERFILE}" ]]; then
    echo "ERROR: Required file not found: ${LOCAL_FIXED_DOCKERFILE}"
    exit 1
fi

LOCAL_NGINX_ENV_FILE="$(mktemp)"
cleanup_local_env_file() {
    rm -f "${LOCAL_NGINX_ENV_FILE}" || true
}
trap cleanup_local_env_file EXIT

cat > "${LOCAL_NGINX_ENV_FILE}" <<EOF
NGINX_VERSION=${NGINX_VERSION}
NGINX_CONFIG_PATH=${LOCAL_NGINX_CONFIG}
NGINX_PORT=${NGINX_PORT}
NGINX_LOG_PATH=logs
NGINX_CACHE_PATH=/home/axonivy/marketplace/data/cache
EOF

REMOTE_NGINX_CONFIG_FILE="/tmp/marketplace-nginx.conf"
REMOTE_ENV_FILE="/tmp/marketplace-nginx.env"
REMOTE_DOCKER_COMPOSE_FILE="/tmp/marketplace-nginx-docker-compose.yml"
REMOTE_DOCKERFILE_FILE="/tmp/marketplace-nginx-Dockerfile"
scp "${SSH_OPTS[@]}" "${LOCAL_NGINX_CONFIG}" "${SSH_USER}@${NODE_IP}:${REMOTE_NGINX_CONFIG_FILE}"
scp "${SSH_OPTS[@]}" "${LOCAL_NGINX_ENV_FILE}" "${SSH_USER}@${NODE_IP}:${REMOTE_ENV_FILE}"
scp "${SSH_OPTS[@]}" "${LOCAL_FIXED_DOCKER_COMPOSE}" "${SSH_USER}@${NODE_IP}:${REMOTE_DOCKER_COMPOSE_FILE}"
scp "${SSH_OPTS[@]}" "${LOCAL_FIXED_DOCKERFILE}" "${SSH_USER}@${NODE_IP}:${REMOTE_DOCKERFILE_FILE}"

ssh "${SSH_OPTS[@]}" "${SSH_USER}@${NODE_IP}" \
    "NGINX_VERSION='${NGINX_VERSION}' TARGET_ENV='${TARGET_ENV}' REMOTE_NGINX_CONFIG_FILE='${REMOTE_NGINX_CONFIG_FILE}' REMOTE_ENV_FILE='${REMOTE_ENV_FILE}' REMOTE_DOCKER_COMPOSE_FILE='${REMOTE_DOCKER_COMPOSE_FILE}' REMOTE_DOCKERFILE_FILE='${REMOTE_DOCKERFILE_FILE}' bash -se" <<'REMOTE_EOF'
set -euo pipefail

if [[ "${TARGET_ENV}" == "prod" ]]; then
    REMOTE_NGINX_BASE="/home/axonivy/marketplace/nginx"
else
    REMOTE_NGINX_BASE="/home/axonivy/marketplace/nginx/${TARGET_ENV}"
fi
NEW_RELEASE_PATH="${REMOTE_NGINX_BASE}/${NGINX_VERSION}"
NEW_NGINX_CONFIG_PATH="${NEW_RELEASE_PATH}/nginx.conf"
NEW_NGINX_DOCKER_COMPOSE_PATH="${NEW_RELEASE_PATH}/docker-compose.yml"
NEW_NGINX_DOCKERFILE_PATH="${NEW_RELEASE_PATH}/Dockerfile"
NEW_NGINX_ENV_PATH="${NEW_RELEASE_PATH}/.env"

echo "Configuring nginx ${NGINX_VERSION} on node $(hostname) at ${NEW_RELEASE_PATH}"
mkdir -p "${NEW_RELEASE_PATH}"
cp "${REMOTE_NGINX_CONFIG_FILE}" "${NEW_NGINX_CONFIG_PATH}"
cp "${REMOTE_ENV_FILE}" "${NEW_NGINX_ENV_PATH}"
cp "${REMOTE_DOCKER_COMPOSE_FILE}" "${NEW_NGINX_DOCKER_COMPOSE_PATH}"
cp "${REMOTE_DOCKERFILE_FILE}" "${NEW_NGINX_DOCKERFILE_PATH}"
rm -f "${REMOTE_NGINX_CONFIG_FILE}" || true
rm -f "${REMOTE_ENV_FILE}" || true
rm -f "${REMOTE_DOCKER_COMPOSE_FILE}" || true
rm -f "${REMOTE_DOCKERFILE_FILE}" || true
REMOTE_EOF
