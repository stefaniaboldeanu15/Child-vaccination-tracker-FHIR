#!/usr/bin/env bash
set -euo pipefail

FHIR_CONTAINER_NAME="prt-hapi-fhir"
FHIR_DATA_VOLUME="prt-hapi-data"
FHIR_BASE_URL="http://localhost:8080/fhir"
BACKEND_URL="http://localhost:8081"
FRONTEND_URL="http://localhost:5173"
FHIR_BUNDLE_PATH="backend/seed/practitioner-view-bundle.json"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

DO_SEED=false
RESET_FHIR=false

usage() {
  cat <<EOF
Usage: $0 [--seed] [--reset-fhir]

  --seed        Seed data only if not already seeded
  --reset-fhir  Delete the persistent FHIR data volume (fresh DB)
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --seed) DO_SEED=true; shift ;;
    --reset-fhir) RESET_FHIR=true; shift ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown arg: $1"; usage; exit 1 ;;
  esac
done

cleanup() {
  echo ""
  echo "Stopping..."
  [[ -n "${BACKEND_PID:-}" ]] && kill "$BACKEND_PID" 2>/dev/null || true
  [[ -n "${FRONTEND_PID:-}" ]] && kill "$FRONTEND_PID" 2>/dev/null || true
  docker rm -f "$FHIR_CONTAINER_NAME" >/dev/null 2>&1 || true
}
trap cleanup INT TERM EXIT

http_code() {
  curl -s -o /dev/null -w "%{http_code}" "$1" 2>/dev/null || true
}

json_pretty_print() {
  if command -v python3 >/dev/null 2>&1; then
    python3 -m json.tool 2>/dev/null || cat
  else
    cat
  fi
}

is_bundle_seeded() {
  local response
  response="$(curl -s "${FHIR_BASE_URL}/Practitioner?identifier=dr.mueller" 2>/dev/null || true)"
  [[ -n "$response" ]] || return 1

  python3 - <<'PY' <<<"$response"
import json, sys
try:
    data = json.load(sys.stdin)
    total = data.get("total", 0)
    raise SystemExit(0 if isinstance(total, int) and total > 0 else 1)
except Exception:
    raise SystemExit(1)
PY
}

seed_fhir() {
  local bundle_path="${PROJECT_ROOT}/${FHIR_BUNDLE_PATH}"

  [[ -f "$bundle_path" ]] || { echo "Seed bundle not found: $bundle_path"; exit 1; }

  echo "Uploading transaction bundle: $bundle_path"

  local response
  local status=0
  response="$(
    curl -sS \
      -X POST "${FHIR_BASE_URL}" \
      -H "Content-Type: application/fhir+json" \
      --data-binary "@${bundle_path}" \
      -w $'\n%{http_code}'
  )" || status=$?

  local body http_status
  body="$(printf '%s' "$response" | sed '$d')"
  http_status="$(printf '%s' "$response" | tail -n1)"

  if [[ $status -ne 0 || ! "$http_status" =~ ^2 ]]; then
    echo
    echo "FHIR upload failed."
    if [[ -n "$body" ]]; then
      echo "FHIR error body:"
      printf '%s\n' "$body" | json_pretty_print
    fi
    exit 1
  fi

  echo "FHIR response:"
  printf '%s\n' "$body" | json_pretty_print

  echo "Checking Practitioner dr.mueller..."
  curl -fsS "${FHIR_BASE_URL}/Practitioner?identifier=dr.mueller" | json_pretty_print

  echo "Checking Patient count..."
  curl -fsS "${FHIR_BASE_URL}/Patient" | json_pretty_print
}

run_backend() {
  cd "$PROJECT_ROOT/backend"
  mvn clean install

  if [[ -x "./mvnw" ]]; then
    FHIR_SERVER_BASE_URL="$FHIR_BASE_URL" ./mvnw spring-boot:run
  else
    FHIR_SERVER_BASE_URL="$FHIR_BASE_URL" mvn spring-boot:run
  fi
}

# ---- FHIR persistence (named volume)
if $RESET_FHIR; then
  echo "Reset requested: removing FHIR data volume '$FHIR_DATA_VOLUME'..."
  docker rm -f "$FHIR_CONTAINER_NAME" >/dev/null 2>&1 || true
  docker volume rm -f "$FHIR_DATA_VOLUME" >/dev/null 2>&1 || true
fi
docker volume create "$FHIR_DATA_VOLUME" >/dev/null

# ---- 1) Start (or restart) local HAPI FHIR server in Docker (with persistent volume)
docker rm -f "$FHIR_CONTAINER_NAME" >/dev/null 2>&1 || true

docker run --name "$FHIR_CONTAINER_NAME" --rm -d \
  -p 8080:8080 \
  -e hapi.fhir.fhir_version=R5 \
  -v "${FHIR_DATA_VOLUME}:/data/hapi" \
  hapiproject/hapi:latest >/dev/null

echo "Waiting for FHIR server at ${FHIR_BASE_URL} ..."
until curl -fsS "${FHIR_BASE_URL}/metadata" >/dev/null 2>&1; do
  sleep 5
done
echo "FHIR server is up."

# ---- 1b) Seed once (optional)
if $DO_SEED; then
  if is_bundle_seeded; then
    echo "Seed skipped (marker exists: Practitioner identifier dr.mueller)."
  else
    seed_fhir
  fi
fi

# ---- 2) Start backend (points to local FHIR)
echo "Starting backend..."
run_backend &
BACKEND_PID=$!

echo "Waiting for backend at ${BACKEND_URL} ..."
until curl -fsS "${BACKEND_URL}/v3/api-docs" >/dev/null 2>&1; do
  sleep 5
done
echo "Backend is up."

# ---- 3) Start frontend
echo "Starting frontend..."
(
  cd "$PROJECT_ROOT/frontend"
  if [[ ! -d node_modules ]]; then
    npm install
  fi
  npm run dev
) &
FRONTEND_PID=$!

echo ""
echo "Running:"
echo "  FHIR:     ${FHIR_BASE_URL}"
echo "  Backend:  ${BACKEND_URL}"
echo "  Frontend: ${FRONTEND_URL}"
echo ""
echo "Press Ctrl+C to stop everything."

wait -n "$BACKEND_PID" "$FRONTEND_PID"
