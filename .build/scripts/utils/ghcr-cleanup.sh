#!/usr/bin/env bash
# Cleans old GHCR container image versions and keeps only the latest retained versions.
set -euo pipefail

# Validates required environment variables and dependencies.
validate_inputs() {
    : "${IMAGE_NAME:?IMAGE_NAME is required}"
    : "${GH_TOKEN:?GH_TOKEN is required}"
    : "${GITHUB_REPOSITORY_OWNER:?GITHUB_REPOSITORY_OWNER is required}"
    : "${VERSION_RETENTION_COUNT:?VERSION_RETENTION_COUNT is required}"

    echo "DEBUG: IMAGE_NAME=${IMAGE_NAME} GITHUB_REPOSITORY_OWNER=${GITHUB_REPOSITORY_OWNER} VERSION_RETENTION_COUNT=${VERSION_RETENTION_COUNT}" >&2

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
    local response
    versions_url="https://api.github.com/orgs/${GITHUB_REPOSITORY_OWNER}/packages/container/${IMAGE_NAME}/versions?per_page=100"

    echo "DEBUG: Calling GET ${versions_url}" >&2

    response="$(curl -sS -w "%{http_code}" \
        -H "Authorization: Bearer ${GH_TOKEN}" \
        -H "Accept: application/vnd.github+json" \
        "${versions_url}")"

    echo "DEBUG: Full response body: ${response::-3}" >&2

    echo "${response}"
}

# Deletes a single GHCR package version id.
delete_version_by_id() {
    local version_id="$1"
    local delete_url
    local http_code

    delete_url="https://api.github.com/orgs/${GITHUB_REPOSITORY_OWNER}/packages/container/${IMAGE_NAME}/versions/${version_id}"

    echo "DEBUG: Calling DELETE ${delete_url}" >&2

    http_code="$(curl -sS -o /dev/null -w "%{http_code}" -X DELETE -H "Authorization: Bearer ${GH_TOKEN}" "${delete_url}")"

    echo "DEBUG: DELETE ${version_id} returned HTTP ${http_code}" >&2

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

# Untagged versions are orphaned digests left behind by re-tagging (e.g. builds reusing the same tag); always remove them.
untagged_ids="$(echo "${versions_json}" | jq -r '.[] | select((.metadata.container.tags // []) | length == 0) | .id')"

# Tagged versions are pruned by recency, keeping only VERSION_RETENTION_COUNT.
tagged_ids_sorted="$(echo "${versions_json}" | jq -r '[.[] | select((.metadata.container.tags // []) | length > 0)] | sort_by(.created_at) | reverse | .[].id')"
tagged_total="$(echo "${tagged_ids_sorted}" | grep -c . || true)"

old_tagged_ids=""
if [[ "${tagged_total}" -gt "${VERSION_RETENTION_COUNT}" ]]; then
    delete_count=$((tagged_total - VERSION_RETENTION_COUNT))
    old_tagged_ids="$(echo "${tagged_ids_sorted}" | tail -n "${delete_count}")"
fi

untagged_count="$(echo "${untagged_ids}" | grep -c . || true)"
old_tagged_count="$(echo "${old_tagged_ids}" | grep -c . || true)"

echo "Tagged versions: ${tagged_total} (keeping ${VERSION_RETENTION_COUNT} most recent)."
echo "Untagged versions to delete: ${untagged_count}"
echo "Old tagged versions to delete: ${old_tagged_count}"

delete_ids="$(printf '%s\n%s' "${untagged_ids}" "${old_tagged_ids}" | sort -u)"

while IFS= read -r id; do
    [[ -n "${id}" ]] || continue
    echo "Deleting version ID: ${id}"
    delete_version_by_id "${id}"
done <<< "${delete_ids}"

echo "Cleanup complete."