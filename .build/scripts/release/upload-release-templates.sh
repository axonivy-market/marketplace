#!/usr/bin/env bash
# Uploads template env/compose files and secret env to a temporary directory on the target node.
set -euo pipefail

NODE_IP="${1:-}"
RELEASE_VERSION="${2:-}"
ENV_SECRET_FILE="${3:-}"
WORKSPACE_ROOT="${4:-}"
REMOTE_TEMPLATE_DIR="${5:-}"

missing_args=()
[[ -z "${NODE_IP}" ]] && missing_args+=("NODE_IP")
[[ -z "${RELEASE_VERSION}" ]] && missing_args+=("RELEASE_VERSION")
[[ -z "${ENV_SECRET_FILE}" ]] && missing_args+=("ENV_SECRET_FILE")
[[ -z "${WORKSPACE_ROOT}" ]] && missing_args+=("WORKSPACE_ROOT")
[[ -z "${REMOTE_TEMPLATE_DIR}" ]] && missing_args+=("REMOTE_TEMPLATE_DIR")
if [[ ${#missing_args[@]} -gt 0 ]]; then
    echo "ERROR: $0 Missing required arguments: ${missing_args[*]}"
    echo "Usage: $0 <NODE_IP> <RELEASE_VERSION> <ENV_SECRET_FILE> <WORKSPACE_ROOT> <REMOTE_TEMPLATE_DIR>"
    exit 1
fi

SSH_USER="${SSH_REMOTE_USER:-ec2-user}"
SSH_OPTS=( -o StrictHostKeyChecking=accept-new -o ConnectTimeout=10 -o UserKnownHostsFile=~/.ssh/known_hosts )
if [[ -n "${SSH_PRIVATE_KEY_FILE:-}" ]]; then
    SSH_OPTS+=( -i "${SSH_PRIVATE_KEY_FILE}" )
fi

TEMPLATE_ROOT="${WORKSPACE_ROOT}/target-source/marketplace-build/templates"
LOCAL_TEMPLATE_ENV="${TEMPLATE_ROOT}/.env"
LOCAL_TEMPLATE_COMPOSE="${TEMPLATE_ROOT}/docker-compose.yml"

for required_file in "${LOCAL_TEMPLATE_ENV}" "${LOCAL_TEMPLATE_COMPOSE}" "${ENV_SECRET_FILE}"; do
    if [[ ! -f "${required_file}" ]]; then
        echo "ERROR: Required file not found: ${required_file}"
        exit 1
    fi
done

echo "Preparing remote temporary directory: ${REMOTE_TEMPLATE_DIR}"
ssh "${SSH_OPTS[@]}" "${SSH_USER}@${NODE_IP}" "mkdir -p '${REMOTE_TEMPLATE_DIR}'"

echo "Uploading template and secret files to ${NODE_IP}"
scp "${SSH_OPTS[@]}" "${LOCAL_TEMPLATE_ENV}" "${SSH_USER}@${NODE_IP}:${REMOTE_TEMPLATE_DIR}/template.env"
scp "${SSH_OPTS[@]}" "${LOCAL_TEMPLATE_COMPOSE}" "${SSH_USER}@${NODE_IP}:${REMOTE_TEMPLATE_DIR}/template.compose.yml"
scp "${SSH_OPTS[@]}" "${ENV_SECRET_FILE}" "${SSH_USER}@${NODE_IP}:${REMOTE_TEMPLATE_DIR}/secret.env"
