#!/usr/bin/env bash
# Rollout step 3: promote the release by switching current, reloading nginx, and cleaning old resources.
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/release-context-lib.sh"

echo "--- Step 3: Promote Release ---"
echo "Switching current symlink to ${NEW_RELEASE_NAME}..."
ln -sfn "${NEW_RELEASE_PATH}" "${CURRENT_LINK}"

echo "Reloading nginx..."
reload_nginx_for_env

if [[ -n "${OLD_RELEASE_NAME}" && "${OLD_RELEASE_NAME}" != "${NEW_RELEASE_NAME}" && -f "${OLD_PUBLISH_PATH}/docker-compose.yml" ]]; then
    echo "Cleaning up old release ${OLD_RELEASE_NAME}..."
    docker compose -f "${OLD_PUBLISH_PATH}/docker-compose.yml" -p "${OLD_COMPOSE_PROJECT}" --env-file "${OLD_PUBLISH_PATH}/.env" down 2>/dev/null || true
    echo "Old release stopped"
fi

echo "Pruning unused Docker images..."
docker image prune -af --filter "until=24h" || true

echo "Promotion of ${NEW_RELEASE_NAME} complete"