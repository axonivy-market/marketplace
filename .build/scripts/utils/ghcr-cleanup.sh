#!/usr/bin/env bash
# Cleans old GHCR container image versions and keeps only the latest retained versions.
set -euo pipefail

# Validates required environment variables and dependencies.
validate_inputs() {
    : "${IMAGE_NAME:?IMAGE_NAME is required}"
    : "${GH_TOKEN:?GH_TOKEN is required}"
    : "${GITHUB_REPOSITORY_OWNER:?GITHUB_REPOSITORY_OWNER is required}"
    : "${VERSION_RETENTION_COUNT:?VERSION_RETENTION_COUNT is required}"

    if ! [[ "${VERSION_RETENTION_COUNT}" =~ ^[0-9]+$ ]]; then
        echo "ERROR: VERSION_RETENTION_COUNT must be a non-negative integer"
        exit 1
    fi

    if ! command -v jq >/dev/null 2>&1; then
        echo "ERROR: jq is required but not installed"
        exit 1
    fi
}

# Retrieves all image versions and returns the response body plus HTTP code suffix.
fetch_versions_response() {
    local versions_url
    versions_url="https://api.github.com/orgs/${GITHUB_REPOSITORY_OWNER}/packages/container/${IMAGE_NAME}/versions?per_page=100"

    curl -sS -w "%{http_code}" \
        -H "Authorization: Bearer ${GH_TOKEN}" \
        "${versions_url}"
}

# Deletes a single GHCR package version id.
delete_version_by_id() {
    local version_id="$1"
    local delete_url
    local http_code

    delete_url="https://api.github.com/orgs/${GITHUB_REPOSITORY_OWNER}/packages/container/${IMAGE_NAME}/versions/${version_id}"
    http_code="$(curl -sS -o /dev/null -w "%{http_code}" -X DELETE -H "Authorization: Bearer ${GH_TOKEN}" "${delete_url}")"

    if [[ "${http_code}" != "204" ]]; then
        echo "WARN: Failed to delete version ID ${version_id} (HTTP ${http_code})"
    fi
}

validate_inputs

echo "Fetching versions of ${IMAGE_NAME} from GHCR..."
response="$(fetch_versions_response)"

status="${response: -3}"
versions_json="${response::-3}"

if [[ "${status}" != "200" ]]; then
    echo "GitHub API request failed (HTTP ${status})."
    exit 1
fi

if [[ "$(echo "${versions_json}" | jq 'length')" -eq 0 ]]; then
    echo "No versions found for ${IMAGE_NAME}."
    exit 0
fi

version_ids="$(echo "${versions_json}" | jq -r 'sort_by(.created_at) | reverse | .[].id')"
total="$(echo "${version_ids}" | wc -l | tr -d '[:space:]')"
delete_count=$((total - VERSION_RETENTION_COUNT))

echo "Total versions found: ${total}"
if [[ "${delete_count}" -le 0 ]]; then
    echo "Nothing to delete. Keeping all ${total} versions."
    exit 0
fi

delete_ids="$(echo "${version_ids}" | tail -n "${delete_count}")"
echo "Deleting ${delete_count} old version(s), keeping ${VERSION_RETENTION_COUNT} most recent..."

while IFS= read -r id; do
    [[ -n "${id}" ]] || continue
    echo "Deleting version ID: ${id}"
    delete_version_by_id "${id}"
done <<< "${delete_ids}"

echo "Cleanup complete."