#!/usr/bin/env bash
set -euo pipefail

FHIR_CONTAINER_NAME="prt-hapi-fhir"
FHIR_DATA_VOLUME="prt-hapi-data"
FHIR_BASE_URL="http://localhost:8080/fhir"
BACKEND_URL="http://localhost:8081"
FRONTEND_URL="http://localhost:5173"
FHIR_BUNDLE_PATH="backend/seed/practitioner-view-bundle.json"
HAPI_IMAGE="${HAPI_IMAGE:-hapiproject/hapi:v8.8.0-1}"
FHIR_STARTUP_TIMEOUT_SECS="${FHIR_STARTUP_TIMEOUT_SECS:-120}"
BACKEND_STARTUP_TIMEOUT_SECS="${BACKEND_STARTUP_TIMEOUT_SECS:-120}"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

DO_SEED=false
RESET_FHIR=false
DO_BUILD=false

usage() {
  cat <<EOF2
Usage: $0 [--seed] [--reset-fhir] [--build]

  --seed        Seed data only if not already seeded
  --reset-fhir  Delete the persistent FHIR data volume (fresh DB)
  --build       Run 'mvn -DskipTests package' before starting the backend
EOF2
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --seed) DO_SEED=true; shift ;;
    --reset-fhir) RESET_FHIR=true; shift ;;
    --build) DO_BUILD=true; shift ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown arg: $1" >&2; usage; exit 1 ;;
  esac
done

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing required command: $1" >&2
    exit 1
  }
}

json_pretty_print() {
  if command -v python3 >/dev/null 2>&1; then
    python3 -m json.tool 2>/dev/null || cat
  else
    cat
  fi
}

cleanup() {
  local exit_code=$?
  trap - INT TERM EXIT

  echo ""
  echo "Stopping..."

  [[ -n "${BACKEND_PID:-}" ]] && kill -- -"${BACKEND_PID}" 2>/dev/null || true
  [[ -n "${FRONTEND_PID:-}" ]] && kill -- -"${FRONTEND_PID}" 2>/dev/null || true
  docker rm -f "$FHIR_CONTAINER_NAME" >/dev/null 2>&1 || true

  exit "$exit_code"
}
trap cleanup INT TERM EXIT

wait_for_url() {
  local name="$1"
  local url="$2"
  local timeout_secs="$3"
  local pid="${4:-}"
  local elapsed=0

  echo "Waiting for ${name} at ${url} ..."

  while (( elapsed < timeout_secs )); do
    if curl -fsS "$url" >/dev/null 2>&1; then
      echo "${name} is up."
      return 0
    fi

    if [[ -n "$pid" ]] && ! kill -0 "$pid" 2>/dev/null; then
      echo "${name} exited before becoming ready." >&2
      return 1
    fi

    sleep 1
    elapsed=$((elapsed + 1))
  done

  echo "Timed out waiting for ${name} after ${timeout_secs}s." >&2
  return 1
}

is_bundle_seeded() {
  curl -fsS "${FHIR_BASE_URL}/Practitioner?identifier=dr.mueller" \
    | python3 -c '
import json, sys
try:
    data = json.load(sys.stdin)
except Exception:
    raise SystemExit(1)
raise SystemExit(0 if data.get("total", 0) > 0 else 1)
'
}

seed_fhir() {
  local bundle_path="${PROJECT_ROOT}/${FHIR_BUNDLE_PATH}"

  [[ -f "$bundle_path" ]] || {
    echo "Seed bundle not found: $bundle_path" >&2
    exit 1
  }

  echo "Uploading transaction bundle: $bundle_path"

  local response
  local status=0
  response="$({
    curl -sS \
      -X POST "${FHIR_BASE_URL}" \
      -H "Content-Type: application/fhir+json" \
      --data-binary "@${bundle_path}" \
      -w $'\n%{http_code}'
  })" || status=$?

  local body http_status
  body="$(printf '%s' "$response" | sed '$d')"
  http_status="$(printf '%s' "$response" | tail -n1)"

  if [[ $status -ne 0 || ! "$http_status" =~ ^2 ]]; then
    echo ""
    echo "FHIR upload failed." >&2
    if [[ -n "$body" ]]; then
      echo "FHIR error body:" >&2
      printf '%s\n' "$body" | json_pretty_print >&2
    fi
    exit 1
  fi

  echo "FHIR response:"
  printf '%s\n' "$body" | json_pretty_print

  echo "Checking Practitioner dr.mueller..."
  curl -fsS "${FHIR_BASE_URL}/Practitioner?identifier=app:username|dr.mueller" | json_pretty_print

  echo "Checking Patient count..."
  curl -fsS "${FHIR_BASE_URL}/Patient" | json_pretty_print
}

build_backend() {
  echo "Building backend..."
  (
    cd "$PROJECT_ROOT/backend"
    mvn -DskipTests package
  )
}

start_backend() {
  echo "Starting backend..."
  setsid bash -c '
    cd "$1"
    exec env FHIR_SERVER_BASE_URL="$2" mvn -DskipTests spring-boot:run
  ' bash "$PROJECT_ROOT/backend" "$FHIR_BASE_URL" &
  BACKEND_PID=$!
}

start_frontend() {
  echo "Starting frontend..."
  setsid bash -c '
    cd "$1"

    if [[ ! -d node_modules || ( -f package-lock.json && package-lock.json -nt node_modules ) ]]; then
      if [[ -f package-lock.json ]]; then
        npm ci
      else
        npm install
      fi
    fi

    exec npm run dev
  ' bash "$PROJECT_ROOT/frontend" &
  FRONTEND_PID=$!
}

require_cmd docker
require_cmd curl
require_cmd npm
require_cmd mvn

if $DO_SEED; then
  require_cmd python3
fi

docker info >/dev/null 2>&1 || {
  echo "Docker is not running or not reachable." >&2
  exit 1
}

if $RESET_FHIR; then
  echo "Reset requested: removing FHIR data volume '$FHIR_DATA_VOLUME'..."
  docker rm -f "$FHIR_CONTAINER_NAME" >/dev/null 2>&1 || true
  docker volume rm -f "$FHIR_DATA_VOLUME" >/dev/null 2>&1 || true
fi

docker volume create "$FHIR_DATA_VOLUME" >/dev/null

docker rm -f "$FHIR_CONTAINER_NAME" >/dev/null 2>&1 || true

docker run --name "$FHIR_CONTAINER_NAME" --rm -d \
  -p 8080:8080 \
  -e hapi.fhir.fhir_version=R5 \
  -v "${FHIR_DATA_VOLUME}:/data/hapi" \
  "$HAPI_IMAGE" >/dev/null

if ! wait_for_url "FHIR server" "${FHIR_BASE_URL}/metadata" "$FHIR_STARTUP_TIMEOUT_SECS"; then
  echo "FHIR container logs:" >&2
  docker logs --tail 200 "$FHIR_CONTAINER_NAME" >&2 || true
  exit 1
fi

if $DO_SEED; then
  if is_bundle_seeded; then
    echo "Seed skipped (marker exists: Practitioner identifier dr.mueller)."
  else
    seed_fhir
  fi
fi

if $DO_BUILD; then
  build_backend
fi

start_backend
if ! wait_for_url "Backend" "${BACKEND_URL}/v3/api-docs" "$BACKEND_STARTUP_TIMEOUT_SECS" "$BACKEND_PID"; then
  wait "$BACKEND_PID" || true
  exit 1
fi

start_frontend

echo ""
echo "Running:"
echo "  FHIR:     ${FHIR_BASE_URL}"
echo "  Backend:  ${BACKEND_URL}"
echo "  Frontend: ${FRONTEND_URL}"
echo ""
echo "Press Ctrl+C to stop everything."

wait -n "$BACKEND_PID" "$FRONTEND_PID"
