#!/usr/bin/env bash
# Orchestrates release configuration setup by running template upload and workspace/compose preparation steps.
set -euo pipefail

NODE_IP="${1:-}"
RELEASE_VERSION="${2:-}"
MARKET_NODE_NUMBER="${3:-}"
ENV_SECRET_FILE="${4:-}"

missing_args=()
[[ -z "$NODE_IP" ]] && missing_args+=("NODE_IP")
[[ -z "$RELEASE_VERSION" ]] && missing_args+=("RELEASE_VERSION")
[[ -z "$ENV_SECRET_FILE" ]] && missing_args+=("ENV_SECRET_FILE")
if [[ ${#missing_args[@]} -gt 0 ]]; then
    echo "ERROR: $0 Missing required arguments: ${missing_args[*]}"
    echo "Usage: $0 <NODE_IP> <RELEASE_VERSION> [MARKET_NODE_NUMBER] <ENV_SECRET_FILE>"
    exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="${WORKSPACE_ROOT:-$(cd "${SCRIPT_DIR}/../../../../" && pwd)}"
REMOTE_TEMPLATE_DIR="/tmp/marketplace-template-${RELEASE_VERSION}-$$"

SSH_USER="${SSH_REMOTE_USER:-ec2-user}"
SSH_OPTS=( -o StrictHostKeyChecking=accept-new -o ConnectTimeout=10 -o UserKnownHostsFile=~/.ssh/known_hosts )
if [[ -n "${SSH_PRIVATE_KEY_FILE:-}" ]]; then
    SSH_OPTS+=( -i "${SSH_PRIVATE_KEY_FILE}" )
fi

# Removes temporary template assets on the remote host when this script exits.
cleanup_remote_templates() {
    ssh "${SSH_OPTS[@]}" "${SSH_USER}@${NODE_IP}" "rm -rf '${REMOTE_TEMPLATE_DIR}'" >/dev/null 2>&1 || true
}
trap cleanup_remote_templates EXIT

echo "Release: ${RELEASE_VERSION}"

"${SCRIPT_DIR}/upload-release-templates.sh" "${NODE_IP}" "${RELEASE_VERSION}" "${ENV_SECRET_FILE}" "${WORKSPACE_ROOT}" "${REMOTE_TEMPLATE_DIR}"
"${SCRIPT_DIR}/prepare-release-workspace.sh" "${NODE_IP}" "${RELEASE_VERSION}" "${MARKET_NODE_NUMBER}" "${REMOTE_TEMPLATE_DIR}"
"${SCRIPT_DIR}/prepare-release-compose-assets.sh" "${NODE_IP}" "${RELEASE_VERSION}" "${REMOTE_TEMPLATE_DIR}"
echo "Config location: /home/axonivy/marketplace/releases/${RELEASE_VERSION}/publish/.env"