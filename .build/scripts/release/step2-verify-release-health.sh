#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/release-context-lib.sh"

: "${HEALTH_CHECK_TARGETS:?HEALTH_CHECK_TARGETS is required}"

IFS=',' read -r -a RAW_HEALTH_TARGETS <<< "${HEALTH_CHECK_TARGETS}"
HEALTH_TARGETS_LIST=()
for raw_target in "${RAW_HEALTH_TARGETS[@]}"; do
    target="$(echo "${raw_target}" | xargs)"
    [[ -z "${target}" ]] && continue

    if ! [[ "${target}" =~ ^[A-Za-z0-9._-]+/[A-Za-z0-9._-]+$ ]]; then
        echo "ERROR: Invalid health check target: ${target}. Expected {service-name}/{app-name}"
        exit 1
    fi

    HEALTH_TARGETS_LIST+=("${target}")
done

if [[ "${#HEALTH_TARGETS_LIST[@]}" -eq 0 ]]; then
    echo "ERROR: No valid health check targets provided"
    exit 1
fi

echo "--- Step 2: Health Check ---"
echo "Checking /actuator/health for targets: ${HEALTH_TARGETS_LIST[*]}..."

check_health_from_container() {
    local container_id="$1"
    local health_path="$2"
    local nonce="$3"
    local health_target_label="$4"

    local ip
    local port
    local port_source
    local health_url
    local response

    ip="$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' "${container_id}" 2>/dev/null || true)"
    if [[ -z "${ip}" ]]; then
        echo NO_IP
        return 0
    fi
    echo "[health-check] target=${health_target_label} container_id=${container_id} ip=${ip}"

    port="$(docker inspect -f '{{range .Config.Env}}{{println .}}{{end}}' "${container_id}" 2>/dev/null | awk -F= '$1=="ACTUATOR_PORT"{print $2;exit}')"
    port_source="ACTUATOR_PORT"
    if [[ -z "${port}" ]]; then
        port="$(docker inspect -f '{{range .Config.Env}}{{println .}}{{end}}' "${container_id}" 2>/dev/null | awk -F= '$1=="SERVER_PORT"{print $2;exit}')"
        port_source="SERVER_PORT"
    fi
    if [[ -z "${port}" ]]; then
        port=8080
        port_source="default"
    fi
    echo "[health-check] target=${health_target_label} port=${port} source=${port_source}"

    health_url="http://${ip}:${port}${health_path}?_nocache=${nonce}"
    echo "[health-check] target=${health_target_label} url=${health_url}"

    if command -v curl >/dev/null 2>&1; then
        response="$(curl -sf "${health_url}" 2>/dev/null || true)"
    elif command -v wget >/dev/null 2>&1; then
        response="$(wget -qO- "${health_url}" 2>/dev/null || true)"
    else
        echo NO_HTTP_CLIENT
        return 0
    fi

    printf '%s' "${response}" | grep -o '"status"[[:space:]]*:[[:space:]]*"[A-Z]*"' | head -n1 | cut -d'"' -f4
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
        service_name="${health_target%%/*}"
        app_name="${health_target#*/}"
        container_id="$(docker compose -f "${NEW_PUBLISH_PATH}/docker-compose.yml" -p "${NEW_COMPOSE_PROJECT}" --env-file "${NEW_PUBLISH_PATH}/.env" ps -q "${service_name}" | head -n1)"

        if [[ -z "${container_id}" ]]; then
            ALL_HEALTHY=false
            PENDING_STATUS+=("${health_target}:container-not-found")
            continue
        fi

        if [[ "${app_name}" == "ROOT" ]]; then
            health_path="/actuator/health"
        else
            health_path="/${app_name}/actuator/health"
        fi

        HEALTH="$(check_health_from_container "${container_id}" "${health_path}" "$(date +%s%N)-$$" "${health_target}")"
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
    echo "Health check failed for ${NEW_RELEASE_NAME}. Rolling back..."
    docker compose -f "${NEW_PUBLISH_PATH}/docker-compose.yml" -p "${NEW_COMPOSE_PROJECT}" --env-file "${NEW_PUBLISH_PATH}/.env" down || true

    if [[ -n "${OLD_RELEASE_NAME}" && -f "${OLD_PUBLISH_PATH}/docker-compose.yml" ]]; then
        echo "Restarting old release ${OLD_RELEASE_NAME}..."
        docker compose -f "${OLD_PUBLISH_PATH}/docker-compose.yml" -p "${OLD_COMPOSE_PROJECT}" --env-file "${OLD_PUBLISH_PATH}/.env" up -d || true
    fi
    exit 1
fi

echo "Deployment health check passed"