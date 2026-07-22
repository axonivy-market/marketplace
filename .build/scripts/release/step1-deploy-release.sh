#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/release-context-lib.sh"

echo "--- Step 1: Deploy New Release ---"
echo "Logging into ghcr.io..."
ghcr_login

if [[ -n "${OLD_RELEASE_NAME}" && "${OLD_RELEASE_NAME}" != "${NEW_RELEASE_NAME}" && -f "${OLD_PUBLISH_PATH}/docker-compose.yml" ]]; then
    echo "Stopping old release ${OLD_RELEASE_NAME}..."
    docker compose -f "${OLD_PUBLISH_PATH}/docker-compose.yml" -p "${OLD_COMPOSE_PROJECT}" --env-file "${OLD_PUBLISH_PATH}/.env" down || true
fi

echo "Starting ${NEW_RELEASE_NAME}..."
docker compose -f "${NEW_PUBLISH_PATH}/docker-compose.yml" -p "${NEW_COMPOSE_PROJECT}" --env-file "${NEW_PUBLISH_PATH}/.env" up -d --pull always