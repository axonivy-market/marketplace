#!/usr/bin/env bash
# Shared release rollout context and helpers for compose naming, rollback state, and GHCR login.
set -euo pipefail

: "${RELEASE_VERSION:?RELEASE_VERSION is required}"
: "${CREDS_TEMP_FILE:?CREDS_TEMP_FILE is required}"

REMOTE_BASE="/home/axonivy/marketplace"
RELEASES_PATH="${REMOTE_BASE}/releases"
CURRENT_LINK="${RELEASES_PATH}/current"
NEW_RELEASE_NAME="${RELEASE_VERSION}"
NEW_RELEASE_PATH="${RELEASES_PATH}/${NEW_RELEASE_NAME}"
NEW_PUBLISH_PATH="${NEW_RELEASE_PATH}/publish"
RELEASE_ENV_RAW="${TARGET_ENV:-${RELEASE_ENV:-prod}}"
RELEASE_ENV="$(printf '%s' "${RELEASE_ENV_RAW}" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/-/g; s/^-+//; s/-+$//')"
[[ -n "${RELEASE_ENV}" ]] || RELEASE_ENV="prod"

compose_project_for_release() {
    if [[ "${RELEASE_ENV}" == "preview" ]]; then
        printf 'market-preview-'
    else
        printf 'market-'
    fi
}

load_release_context() {
    NEW_COMPOSE_PROJECT="$(compose_project_for_release)"

    if [[ -n "${ROLLBACK_RELEASE_NAME:-}" ]]; then
        OLD_RELEASE_NAME="${ROLLBACK_RELEASE_NAME}"
        OLD_RELEASE_PATH="${ROLLBACK_RELEASE_PATH:-${RELEASES_PATH}/${OLD_RELEASE_NAME}}"
        OLD_COMPOSE_PROJECT="${ROLLBACK_COMPOSE_PROJECT:-$(compose_project_for_release)}"

        if [[ -n "${ROLLBACK_PUBLISH_PATH:-}" ]]; then
            OLD_PUBLISH_PATH="${ROLLBACK_PUBLISH_PATH}"
        elif [[ -f "${OLD_RELEASE_PATH}/publish/docker-compose.yml" ]]; then
            OLD_PUBLISH_PATH="${OLD_RELEASE_PATH}/publish"
        else
            OLD_PUBLISH_PATH="${OLD_RELEASE_PATH}"
        fi
        return
    fi
    if [[ -L "${CURRENT_LINK}" ]]; then
        OLD_RELEASE_PATH="$(readlink -f "${CURRENT_LINK}")"
        OLD_RELEASE_NAME="$(basename "${OLD_RELEASE_PATH}")"
        OLD_COMPOSE_PROJECT="$(compose_project_for_release)"
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

nginx_compose_project_for_env() {
    if [[ "${RELEASE_ENV}" == "prod" ]]; then
        printf 'market-nginx'
    else
        printf 'market-nginx-%s' "${RELEASE_ENV}"
    fi
}

restart_nginx_for_env() {
    local nginx_project
    local container_ids

    nginx_project="$(nginx_compose_project_for_env)"
    container_ids="$(docker ps -q \
        --filter "label=com.docker.compose.project=${nginx_project}" \
        --filter "label=com.docker.compose.service=nginx" || true)"

    if [[ -z "${container_ids}" ]]; then
        container_ids="$(docker ps -q --filter "label=com.docker.compose.project=${nginx_project}" || true)"
    fi

    if [[ -z "${container_ids}" ]]; then
        echo "Nginx container for project ${nginx_project} not found, skipping restart"
        return 0
    fi

    echo "Restarting nginx container for project ${nginx_project}..."
    docker restart ${container_ids} >/dev/null
    echo "Nginx restarted"
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