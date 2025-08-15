#!/bin/bash
set -euo pipefail

echo "Fetching versions of $IMAGE_NAME from GHCR..."

response=$(curl -s -w "%{http_code}" \
  -H "Authorization: Bearer $GH_TOKEN" \
  "https://api.github.com/orgs/$GITHUB_REPOSITORY_OWNER/packages/container/$IMAGE_NAME/versions?per_page=100")

# Extract HTTP status code (last 3 chars of response)
STATUS="${response: -3}"

# Extract JSON body (everything except the last 3 chars)
VERSIONS="${response::-3}"

# Check for success
if [[ "$STATUS" != "200" ]]; then
  echo "GitHub API request failed (HTTP $STATUS)."
  exit 1
fi
if [[ $(echo "$VERSIONS" | jq 'length') -eq 0 ]]; then
  echo "No versions found for $IMAGE_NAME."
  exit 0
fi

# Get IDs of the latest N tagged versions to keep
KEEP_IDS=$(echo "$VERSIONS" \
  | jq -r '[.[] | select(.metadata.container.tags | length > 0)]
            | sort_by(.created_at)
            | reverse
            | .[:'"$VERSION_RETENTION_COUNT"']
            | .[].id')

echo "Keeping version IDs:"
echo "$KEEP_IDS"

# Loop all versions and delete if not in KEEP_IDS
ALL_IDS=$(echo "$VERSIONS" | jq -r '.[].id')
echo "all IDs:"
echo "$ALL_IDS"
for id in $ALL_IDS; do
  id=$(echo "$id" | tr -d '\r\n')
  if echo "$KEEP_IDS" | grep -qx "$id"; then
    echo "Skipping version ID: $id (kept)"
  else
    echo "Deleting version ID: $id"
    url="https://api.github.com/orgs/$GITHUB_REPOSITORY_OWNER/packages/container/$IMAGE_NAME/versions/$id"
    response=$(curl -s -X DELETE -H "Authorization: Bearer $GH_TOKEN" "$url")
    echo "Response: $response"
  fi
done

echo "Cleanup complete."