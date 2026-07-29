#!/usr/bin/env bash
# Rollout step 3: promote the release by switching current, reloading nginx, and cleaning old resources.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/release-context-lib.sh"

echo "--- Step 3: Promote Release ---"
echo "Switching current symlink to ${NEW_RELEASE_NAME}..."
ln -sfn "${NEW_RELEASE_PATH}" "${CURRENT_LINK}"

echo "Reloading nginx..."
if docker ps --format '{{.Names}}' | grep -q 'nginx'; then
    CONTAINER_ID="$(docker ps -qf 'name=nginx' | head -n1)"
    if [[ -n "${CONTAINER_ID}" ]]; then
        docker exec "${CONTAINER_ID}" nginx -s reload || true
        echo "Nginx reloaded"
    fi
elif command -v nginx >/dev/null 2>&1; then
    nginx -s reload || true
    echo "Nginx reloaded"
else
    echo "Nginx not found, skipping reload"
fi

if [[ -n "${OLD_RELEASE_NAME}" && "${OLD_RELEASE_NAME}" != "${NEW_RELEASE_NAME}" && -f "${OLD_PUBLISH_PATH}/docker-compose.yml" ]]; then
    echo "Cleaning up old release ${OLD_RELEASE_NAME}..."
    docker compose -f "${OLD_PUBLISH_PATH}/docker-compose.yml" -p "${OLD_COMPOSE_PROJECT}" --env-file "${OLD_PUBLISH_PATH}/.env" down 2>/dev/null || true
    echo "Old release stopped"
fi

echo "Pruning unused Docker images..."
docker image prune -af --filter "until=24h" || true

echo "Promotion of ${NEW_RELEASE_NAME} complete"