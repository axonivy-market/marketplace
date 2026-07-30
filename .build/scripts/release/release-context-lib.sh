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

sanitize_compose_segment() {
    local value="$1"

    value="$(printf '%s' "${value}" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/-/g; s/^-+//; s/-+$//; s/-+/-/g')"
    printf '%s' "${value}"
}

RELEASE_ENV_RAW="${TARGET_ENV:-${RELEASE_ENV:-prod}}"
RELEASE_ENV="$(sanitize_compose_segment "${RELEASE_ENV_RAW}")"
[[ -n "${RELEASE_ENV}" ]] || RELEASE_ENV="prod"
COMPOSE_SERVICE_NAME_RAW="${COMPOSE_SERVICE_NAME:-app}"
COMPOSE_SERVICE_NAME="$(sanitize_compose_segment "${COMPOSE_SERVICE_NAME_RAW}")"
[[ -n "${COMPOSE_SERVICE_NAME}" ]] || COMPOSE_SERVICE_NAME="app"

compose_project_name() {
    local target_env="$1"
    local service_name="$2"
    local version="$3"
    local env_value
    local service_value
    local version_value

    env_value="$(sanitize_compose_segment "${target_env}")"
    service_value="$(sanitize_compose_segment "${service_name}")"
    version_value="$(sanitize_compose_segment "${version}")"

    [[ -n "${env_value}" ]] || env_value="prod"
    [[ -n "${service_value}" ]] || service_value="app"
    [[ -n "${version_value}" ]] || version_value="latest"

    if [[ "${env_value}" == "prod" ]]; then
        printf 'market-%s-%s' "${service_value}" "${version_value}"
    else
        printf 'market-%s-%s-%s' "${env_value}" "${service_value}" "${version_value}"
    fi
}

compose_project_for_release() {
    local release_name="${1:-${NEW_RELEASE_NAME}}"
    local service_name="${2:-${COMPOSE_SERVICE_NAME}}"

    compose_project_name "${RELEASE_ENV}" "${service_name}" "${release_name}"
}

load_release_context() {
    NEW_COMPOSE_PROJECT="$(compose_project_for_release "${NEW_RELEASE_NAME}")"

    if [[ -n "${ROLLBACK_RELEASE_NAME:-}" ]]; then
        OLD_RELEASE_NAME="${ROLLBACK_RELEASE_NAME}"
        OLD_RELEASE_PATH="${ROLLBACK_RELEASE_PATH:-${RELEASES_PATH}/${OLD_RELEASE_NAME}}"
        OLD_COMPOSE_PROJECT="${ROLLBACK_COMPOSE_PROJECT:-$(compose_project_for_release "${OLD_RELEASE_NAME}")}"

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

nginx_current_link_for_env() {
    if [[ "${RELEASE_ENV}" == "prod" ]]; then
        printf '%s/nginx/current' "${REMOTE_BASE}"
    else
        printf '%s/nginx/%s/current' "${REMOTE_BASE}" "${RELEASE_ENV}"
    fi
}

current_nginx_project_for_env() {
    local nginx_current_link
    local nginx_release_path
    local nginx_release_name

    nginx_current_link="$(nginx_current_link_for_env)"
    [[ -L "${nginx_current_link}" ]] || return 1

    nginx_release_path="$(readlink -f "${nginx_current_link}" 2>/dev/null || true)"
    [[ -n "${nginx_release_path}" ]] || return 1

    nginx_release_name="$(basename "${nginx_release_path}")"
    compose_project_name "${RELEASE_ENV}" 'nginx' "${nginx_release_name}"
}

reload_nginx_for_env() {
    local nginx_project
    local container_id

    if nginx_project="$(current_nginx_project_for_env)"; then
        container_id="$(docker ps -q \
            --filter "label=com.docker.compose.project=${nginx_project}" \
            --filter "label=com.docker.compose.service=nginx" | head -n1 || true)"

        if [[ -n "${container_id}" ]]; then
            echo "Reloading nginx for project ${nginx_project}..."
            docker exec "${container_id}" nginx -s reload || true
            echo "Nginx reloaded"
            return 0
        fi
    fi

    if command -v nginx >/dev/null 2>&1; then
        nginx -s reload || true
        echo "Nginx reloaded"
    else
        echo "Nginx not found, skipping reload"
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