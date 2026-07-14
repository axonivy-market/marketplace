#!/usr/bin/env bash
set -euo pipefail

: "${RELEASE_VERSION:?RELEASE_VERSION is required}"
: "${CREDS_TEMP_FILE:?CREDS_TEMP_FILE is required}"

REMOTE_BASE="/home/axonivy/marketplace"
RELEASES_PATH="${REMOTE_BASE}/releases"
CURRENT_LINK="${RELEASES_PATH}/current"
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

load_release_context() {
    NEW_COMPOSE_PROJECT="$(compose_project_for_release "${NEW_RELEASE_NAME}")"

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
}

ghcr_login() {
    local ghcr_username
    local ghcr_token

    ghcr_username="$(head -n1 "${CREDS_TEMP_FILE}")"
    ghcr_token="$(tail -n1 "${CREDS_TEMP_FILE}")"
    echo "${ghcr_token}" | docker login -u "${ghcr_username}" --password-stdin ghcr.io >/dev/null 2>&1 || {
        echo "Failed to login to ghcr.io"
        exit 1
    }
}

load_release_context