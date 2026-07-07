#!/usr/bin/env bash
# This script uploads the remote deployment steps and runs them in sequence on the EC2 instance.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

NODE_IP="${1:-}"
RELEASE_VERSION="${2:-}"
HEALTH_CHECK_TARGETS_ARG="${3:-}"

if [[ -z "${NODE_IP}" || -z "${RELEASE_VERSION}" || -z "${HEALTH_CHECK_TARGETS_ARG}" ]]; then
    echo "ERROR: $0 Missing required arguments: <NODE_IP> <RELEASE_VERSION> <HEALTH_CHECK_TARGETS_ARG>"
    exit 1
fi

SSH_USER="${SSH_REMOTE_USER:-ec2-user}"
SSH_OPTS=( -o StrictHostKeyChecking=accept-new -o ConnectTimeout=10 -o UserKnownHostsFile=~/.ssh/known_hosts )
if [[ -n "${SSH_PRIVATE_KEY_FILE:-}" ]]; then
    SSH_OPTS+=( -i "${SSH_PRIVATE_KEY_FILE}" )
fi

HEALTH_CHECK_TIMEOUT="${HEALTH_CHECK_TIMEOUT:-300}"
HEALTH_CHECK_INTERVAL="${HEALTH_CHECK_INTERVAL:-10}"
REMOTE_SCRIPT_DIR="/tmp/marketplace-deploy-$(date +%s)-$$"
CREDS_TEMP_FILE="${REMOTE_SCRIPT_DIR}/ghcr-creds"

GHCR_CREDS_FILE="$(mktemp)"
chmod 600 "${GHCR_CREDS_FILE}"
trap 'rm -f "${GHCR_CREDS_FILE}"' EXIT
echo "${GHCR_USERNAME}" > "${GHCR_CREDS_FILE}"
echo "${GHCR_TOKEN}" >> "${GHCR_CREDS_FILE}"

echo "Node: ${NODE_IP}"
echo "Release: ${RELEASE_VERSION}"

ssh "${SSH_OPTS[@]}" "${SSH_USER}@${NODE_IP}" "mkdir -p '${REMOTE_SCRIPT_DIR}'"

scp "${SSH_OPTS[@]}" \
    "${SCRIPT_DIR}/release-context-lib.sh" \
    "${SCRIPT_DIR}/step1-deploy-release.sh" \
    "${SCRIPT_DIR}/step2-verify-release-health.sh" \
    "${SCRIPT_DIR}/step3-promote-release.sh" \
    "${GHCR_CREDS_FILE}" \
    "${SSH_USER}@${NODE_IP}:${REMOTE_SCRIPT_DIR}/" || {
    echo "Failed to transfer deployment assets"
    exit 1
}

ssh "${SSH_OPTS[@]}" "${SSH_USER}@${NODE_IP}" \
    "RELEASE_VERSION='${RELEASE_VERSION}' HEALTH_CHECK_TARGETS='${HEALTH_CHECK_TARGETS_ARG}' CREDS_TEMP_FILE='${CREDS_TEMP_FILE}' HEALTH_CHECK_TIMEOUT='${HEALTH_CHECK_TIMEOUT}' HEALTH_CHECK_INTERVAL='${HEALTH_CHECK_INTERVAL}' SKIP_DEPLOY_RELEASE='${SKIP_DEPLOY_RELEASE:-false}' REMOTE_SCRIPT_DIR='${REMOTE_SCRIPT_DIR}' bash -se" <<'REMOTE_EOF'
set -euo pipefail

cleanup_remote_assets() {
    rm -rf "${REMOTE_SCRIPT_DIR}" 2>/dev/null || true
}

trap cleanup_remote_assets EXIT

if [[ "${SKIP_DEPLOY_RELEASE:-false}" != "true" ]]; then
    bash "${REMOTE_SCRIPT_DIR}/step1-deploy-release.sh"
else
    echo "Skipping step1-deploy-release.sh because containers were already restarted earlier"
fi
bash "${REMOTE_SCRIPT_DIR}/step2-verify-release-health.sh"
bash "${REMOTE_SCRIPT_DIR}/step3-promote-release.sh"
REMOTE_EOF
