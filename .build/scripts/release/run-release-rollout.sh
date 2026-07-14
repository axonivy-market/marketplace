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

release_version_q="$(printf '%q' "${RELEASE_VERSION}")"
health_targets_q="$(printf '%q' "${HEALTH_CHECK_TARGETS_ARG}")"
creds_temp_file_q="$(printf '%q' "${CREDS_TEMP_FILE}")"
health_check_timeout_q="$(printf '%q' "${HEALTH_CHECK_TIMEOUT}")"
health_check_interval_q="$(printf '%q' "${HEALTH_CHECK_INTERVAL}")"
skip_deploy_release_q="$(printf '%q' "${SKIP_DEPLOY_RELEASE:-false}")"
remote_script_dir_q="$(printf '%q' "${REMOTE_SCRIPT_DIR}")"

ssh "${SSH_OPTS[@]}" "${SSH_USER}@${NODE_IP}" \
    "RELEASE_VERSION=${release_version_q} HEALTH_CHECK_TARGETS=${health_targets_q} CREDS_TEMP_FILE=${creds_temp_file_q} HEALTH_CHECK_TIMEOUT=${health_check_timeout_q} HEALTH_CHECK_INTERVAL=${health_check_interval_q} SKIP_DEPLOY_RELEASE=${skip_deploy_release_q} REMOTE_SCRIPT_DIR=${remote_script_dir_q} bash -se" <<'REMOTE_EOF'
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
