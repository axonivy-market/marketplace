#!/usr/bin/env bash
# Rollout step 2: verify service health endpoints and roll back automatically if checks fail.
set -Eeuo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/release-context-lib.sh"

: "${HEALTH_CHECK_TARGETS:?HEALTH_CHECK_TARGETS is required}"

IFS=',' read -r -a RAW_HEALTH_TARGETS <<< "${HEALTH_CHECK_TARGETS}"
HEALTH_TARGETS_LIST=()
for raw_target in "${RAW_HEALTH_TARGETS[@]}"; do
    target="$(echo "${raw_target}" | xargs)"
    [[ -z "${target}" ]] && continue

    if ! [[ "${target}" =~ ^[0-9]+/[A-Za-z0-9._-]+$ ]]; then
        echo "ERROR: Invalid health check target: ${target}. Expected {port}/{app-name} (example: 8080/app)"
        exit 1
    fi

    HEALTH_TARGETS_LIST+=("${target}")
done

if [[ "${#HEALTH_TARGETS_LIST[@]}" -eq 0 ]]; then
    echo "ERROR: No valid health check targets provided"
    exit 1
fi

ROLLBACK_DONE=false

# Stops new release and restores previous release once per failed rollout.
rollback_release() {
    if [[ "${ROLLBACK_DONE}" == "true" ]]; then
        return 0
    fi

    echo "Health check failed for ${NEW_RELEASE_NAME}. Rolling back..."
    docker compose -f "${NEW_PUBLISH_PATH}/docker-compose.yml" -p "${NEW_COMPOSE_PROJECT}" --env-file "${NEW_PUBLISH_PATH}/.env" down || true

    if [[ -n "${OLD_RELEASE_NAME}" && -f "${OLD_PUBLISH_PATH}/docker-compose.yml" ]]; then
        echo "Restarting old release ${OLD_RELEASE_NAME}..."
        OLD_UI_CONTAINER_NAME="$(container_name_for_release_service 'ui' "${OLD_RELEASE_NAME}")"
        OLD_APP_CONTAINER_NAME="$(container_name_for_release_service 'app' "${OLD_RELEASE_NAME}")"
        OLD_STABLE_CONTAINER_NAME="$(container_name_for_release_service 'stable' "${OLD_RELEASE_NAME}")"
        OLD_PUBLISH_CONTAINER_OVERRIDE_FILE="$(mktemp /tmp/market-release-container-name-old.XXXXXX.yml)"
        cat > "${OLD_PUBLISH_CONTAINER_OVERRIDE_FILE}" <<EOF
services:
    ui:
        container_name: ${OLD_UI_CONTAINER_NAME}
    app:
        container_name: ${OLD_APP_CONTAINER_NAME}
    stable:
        container_name: ${OLD_STABLE_CONTAINER_NAME}
EOF
        docker compose -f "${OLD_PUBLISH_PATH}/docker-compose.yml" -f "${OLD_PUBLISH_CONTAINER_OVERRIDE_FILE}" -p "${OLD_COMPOSE_PROJECT}" --env-file "${OLD_PUBLISH_PATH}/.env" up -d || true
        rm -f "${OLD_PUBLISH_CONTAINER_OVERRIDE_FILE}" 2>/dev/null || true
    else
        echo "WARN: Old release info not available; skipped old release restart"
    fi

    ROLLBACK_DONE=true
}

# Fails step 2 with a message after triggering rollback.
fail_step2() {
    local message="$1"
    echo "ERROR: ${message}"
    rollback_release
    exit 1
}

# Handles unexpected command errors and enforces rollback before exit.
on_step2_error() {
    local exit_code="$?"
    local line_no="$1"
    echo "ERROR: Step 2 failed unexpectedly at line ${line_no} (exit=${exit_code})"
    rollback_release
    exit "${exit_code}"
}

echo "--- Step 2: Health Check ---"
echo "Checking /actuator/health for targets: ${HEALTH_TARGETS_LIST[*]}..."
trap 'on_step2_error $LINENO' ERR

# Checks health endpoint status from inside the container network namespace.
check_health_from_container() {
    local container_id="$1"
    local health_path="$2"
    local nonce="$3"
    local health_target_label="$4"
    local expected_port="$5"

    local ip
    local health_url
    local response
    local response_and_code
    local status_code
    local parsed_status

    ip="$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' "${container_id}" 2>/dev/null || true)"
    if [[ -z "${ip}" ]]; then
        echo NO_IP
        return 0
    fi
    echo "[health-check] target=${health_target_label} container_id=${container_id} ip=${ip}" >&2

    health_url="http://${ip}:${expected_port}${health_path}?_nocache=${nonce}"
    echo "[health-check] target=${health_target_label} url=${health_url}" >&2

    if command -v curl >/dev/null 2>&1; then
        # Keep curl failures non-fatal and capture HTTP code for diagnostics.
        response_and_code="$(curl -sS -m 5 -w $'\n%{http_code}' "${health_url}" 2>/dev/null || true)"
        status_code="$(printf '%s' "${response_and_code}" | tail -n1)"
        response="$(printf '%s' "${response_and_code}" | sed '$d')"
    elif command -v wget >/dev/null 2>&1; then
        response="$(wget -qO- "${health_url}" 2>/dev/null || true)"
        status_code="wget-no-http-code"
    else
        echo NO_HTTP_CLIENT
        return 0
    fi

    parsed_status="$(printf '%s' "${response}" | grep -o '"status"[[:space:]]*:[[:space:]]*"[A-Z]*"' | head -n1 | cut -d'"' -f4 || true)"
    if [[ -n "${parsed_status}" ]]; then
        printf '%s' "${parsed_status}"
    else
        echo "[health-check] target=${health_target_label} parse-miss http_code=${status_code:-unknown} body_sample=$(printf '%s' "${response}" | tr '\n' ' ' | cut -c1-200)" >&2
        printf '%s' "UNKNOWN"
    fi
}

START_TIME="$(date +%s)"
HEALTH_GOOD=false

while true; do
    ELAPSED="$(( $(date +%s) - START_TIME ))"
    if [[ "${ELAPSED}" -ge "${HEALTH_CHECK_TIMEOUT}" ]]; then
        break
    fi

    ALL_HEALTHY=true
    PENDING_STATUS=()
    for health_target in "${HEALTH_TARGETS_LIST[@]}"; do
        target_port="${health_target%%/*}"
        app_name="${health_target#*/}"
        echo "[health-check] target=${health_target} using configured port=${target_port} app=${app_name}"

        container_id="$(docker compose -f "${NEW_PUBLISH_PATH}/docker-compose.yml" -p "${NEW_COMPOSE_PROJECT}" --env-file "${NEW_PUBLISH_PATH}/.env" ps -q "${app_name}" 2>/dev/null | head -n1 || true)"

        if [[ -z "${container_id}" ]]; then
            fail_step2 "target=${health_target} container-not-found for service '${app_name}' in compose project '${NEW_COMPOSE_PROJECT}'"
        fi

        health_path="/${app_name}/actuator/health"

        HEALTH="$(check_health_from_container "${container_id}" "${health_path}" "$(date +%s%N)-$$" "${health_target}" "${target_port}")"
        if [[ "${HEALTH}" == "NO_IP" ]]; then
            fail_step2 "target=${health_target} no IP found for container '${container_id}'"
        fi
        if [[ "${HEALTH}" != "UP" ]]; then
            ALL_HEALTHY=false
            PENDING_STATUS+=("${health_target}:${HEALTH:-unknown}")
        fi
    done

    if [[ "${ALL_HEALTHY}" == "true" ]]; then
        HEALTH_GOOD=true
        echo "Health check passed on all targets: ${HEALTH_TARGETS_LIST[*]}"
        break
    fi

    echo "Health check pending: ${PENDING_STATUS[*]} (${ELAPSED}s/${HEALTH_CHECK_TIMEOUT}s)"
    sleep "${HEALTH_CHECK_INTERVAL}"
done

if [[ "${HEALTH_GOOD}" != "true" ]]; then
    rollback_release
    exit 1
fi

trap - ERR
echo "Deployment health check passed"