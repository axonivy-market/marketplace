#!/usr/bin/env bash
# Creates release compose assets by copying the template compose file into the new publish path.
set -euo pipefail

NODE_IP="${1:-}"
RELEASE_VERSION="${2:-}"
REMOTE_TEMPLATE_DIR="${3:-}"

missing_args=()
[[ -z "${NODE_IP}" ]] && missing_args+=("NODE_IP")
[[ -z "${RELEASE_VERSION}" ]] && missing_args+=("RELEASE_VERSION")
[[ -z "${REMOTE_TEMPLATE_DIR}" ]] && missing_args+=("REMOTE_TEMPLATE_DIR")
if [[ ${#missing_args[@]} -gt 0 ]]; then
    echo "ERROR: $0 Missing required arguments: ${missing_args[*]}"
    echo "Usage: $0 <NODE_IP> <RELEASE_VERSION> <REMOTE_TEMPLATE_DIR>"
    exit 1
fi

SSH_USER="${SSH_REMOTE_USER:-ec2-user}"
SSH_OPTS=( -o StrictHostKeyChecking=accept-new -o ConnectTimeout=10 -o UserKnownHostsFile=~/.ssh/known_hosts )
if [[ -n "${SSH_PRIVATE_KEY_FILE:-}" ]]; then
    SSH_OPTS+=( -i "${SSH_PRIVATE_KEY_FILE}" )
fi

ssh "${SSH_OPTS[@]}" "${SSH_USER}@${NODE_IP}" \
    "RELEASE_VERSION='${RELEASE_VERSION}' REMOTE_TEMPLATE_DIR='${REMOTE_TEMPLATE_DIR}' bash -se" <<'REMOTE_EOF'
set -euo pipefail

NEW_RELEASE_PATH="/home/axonivy/marketplace/releases/${RELEASE_VERSION}"
NEW_PUBLISH_PATH="${NEW_RELEASE_PATH}/publish"

echo "Configuring docker-compose for ${RELEASE_VERSION}"
cp "${REMOTE_TEMPLATE_DIR}/template.compose.yml" "${NEW_PUBLISH_PATH}/docker-compose.yml"
REMOTE_EOF
