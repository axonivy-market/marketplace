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

# Sort versions by created_at descending and extract IDs
VERSION_IDS=$(echo "$VERSIONS" | jq -r 'sort_by(.created_at) | reverse | .[].id')

if [[ $(echo "$VERSIONS" | jq 'length') -eq 0 ]]; then
  echo "No versions found for $IMAGE_NAME."
  exit 0
fi

TOTAL=$(echo "$VERSION_IDS" | wc -l)
DELETE_COUNT=$((TOTAL - VERSION_RETENTION_COUNT))
echo "Total versions found: $TOTAL"
if [[ $DELETE_COUNT -le 0 ]]; then
  echo "Nothing to delete. Keeping all $TOTAL versions."
  exit 0
fi

# Get only the IDs to delete (all but the latest N)
DELETE_IDS=$(echo "$VERSION_IDS" | tail -n "$DELETE_COUNT")

echo "Deleting $DELETE_COUNT old version(s), keeping $VERSION_RETENTION_COUNT most recent..."
for id in $DELETE_IDS; do
  id=$(echo "$id" | tr -d '\r\n')
  echo "Deleting version ID: $id"

  url="https://api.github.com/orgs/$GITHUB_ACTOR/packages/container/$IMAGE_NAME/versions/$id"

  response=$(curl -s -X DELETE -H "Authorization: Bearer $GH_TOKEN" "$url")

  echo "Response: $response"
done

echo "Cleanup complete."