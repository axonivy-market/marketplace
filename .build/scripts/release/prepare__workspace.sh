#!/usr/bin/env bash
# Creates release folders on the node and builds the release .env by merging current, template, and secret values.
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/../../pipeline-lib/ssh-lib.sh"

NODE_IP="${1:-}"
RELEASE_VERSION="${2:-}"
MARKET_NODE_NUMBER="${3:-}"
REMOTE_TEMPLATE_DIR="${4:-}"

missing_args=()
[[ -z "${NODE_IP}" ]] && missing_args+=("NODE_IP")
[[ -z "${RELEASE_VERSION}" ]] && missing_args+=("RELEASE_VERSION")
[[ -z "${REMOTE_TEMPLATE_DIR}" ]] && missing_args+=("REMOTE_TEMPLATE_DIR")
if [[ ${#missing_args[@]} -gt 0 ]]; then
    echo "ERROR: $0 Missing required arguments: ${missing_args[*]}"
    echo "Usage: $0 <NODE_IP> <RELEASE_VERSION> [MARKET_NODE_NUMBER] <REMOTE_TEMPLATE_DIR>"
    exit 1
fi

setup_ssh_opts

ssh "${SSH_OPTS[@]}" "${SSH_USER}@${NODE_IP}" \
    "RELEASE_VERSION='${RELEASE_VERSION}' MARKET_NODE_NUMBER='${MARKET_NODE_NUMBER}' REMOTE_TEMPLATE_DIR='${REMOTE_TEMPLATE_DIR}' bash -se" <<'REMOTE_EOF'
set -euo pipefail

REMOTE_BASE="/home/axonivy/marketplace"
RELEASES_PATH="${REMOTE_BASE}/releases"
CURRENT_LINK="${RELEASES_PATH}/current"
NEW_RELEASE_PATH="${RELEASES_PATH}/${RELEASE_VERSION}"
SHARED_ENV_FILE="${REMOTE_BASE}/shared/.env"

mkdir -p "${NEW_RELEASE_PATH}/logs" "${NEW_RELEASE_PATH}/config/nginx" "${NEW_RELEASE_PATH}/publish"

# Merges current/template/secret env files with latest values taking precedence.
merge_env_files() {
    local current_env="$1"
    local template_env="$2"
    local secret_env="$3"
    local output_env="$4"

    declare -A env_vars

    # Loads key/value pairs from an env file while preserving raw value suffixes.
    load_env_file() {
        local source_file="$1"
        [[ -f "${source_file}" ]] || return 0
        while IFS= read -r line || [[ -n "${line}" ]]; do
            [[ -z "${line}" || "${line}" =~ ^# ]] && continue
            [[ "${line}" == *=* ]] || continue

            local key="${line%%=*}"
            local value="${line#*=}"
            env_vars["${key}"]="${value}"
        done < "${source_file}"
    }

    load_env_file "${current_env}"
    load_env_file "${template_env}"
    load_env_file "${secret_env}"

    env_vars["RELEASE_VERSION"]="${RELEASE_VERSION}"
    env_vars["MARKET_NODE_NUMBER"]="${MARKET_NODE_NUMBER}"

    {
        for key in "${!env_vars[@]}"; do
            echo "${key}=${env_vars[$key]}"
        done | sort
    } > "${output_env}"
    chmod 600 "${output_env}"
}

CURRENT_ENV_FILE=""
if [[ -L "${CURRENT_LINK}" && -f "${CURRENT_LINK}/publish/.env" ]]; then
    CURRENT_ENV_FILE="${CURRENT_LINK}/publish/.env"
elif [[ -L "${CURRENT_LINK}" && -f "${CURRENT_LINK}/.env" ]]; then
    CURRENT_ENV_FILE="${CURRENT_LINK}/.env"
elif [[ -f "${SHARED_ENV_FILE}" ]]; then
    CURRENT_ENV_FILE="${SHARED_ENV_FILE}"
fi

echo "Merging .env for ${RELEASE_VERSION}"
merge_env_files "${CURRENT_ENV_FILE}" "${REMOTE_TEMPLATE_DIR}/template.env" "${REMOTE_TEMPLATE_DIR}/secret.env" "${NEW_RELEASE_PATH}/publish/.env"
REMOTE_EOF
