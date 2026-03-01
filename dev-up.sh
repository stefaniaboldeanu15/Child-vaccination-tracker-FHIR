#!/usr/bin/env bash
set -euo pipefail

FHIR_CONTAINER_NAME="prt-hapi-fhir"
FHIR_DATA_VOLUME="prt-hapi-data"
FHIR_BASE_URL="http://localhost:8080/fhir"
BACKEND_URL="http://localhost:8081"
FRONTEND_URL="http://localhost:5173"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

DO_SEED=false
RESET_FHIR=false

usage() {
  cat <<EOF
Usage: $0 [--seed] [--reset-fhir]

  --seed        Seed mock data only if not already seeded
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
  # Prints HTTP status code or 000 on failure
  curl -sS -o /dev/null -w "%{http_code}" "$1" 2>/dev/null || true
}

seed_fhir() {
  command -v python3 >/dev/null || { echo "python3 is required for seeding."; exit 1; }

  local base="$FHIR_BASE_URL"
  local headers=(-H "Content-Type: application/fhir+json")
  local mock_root="$PROJECT_ROOT/backend/src/main/resources/_mock_data"

  echo "Seeding FHIR from $mock_root ..."

  upload_folder() {
    local type="$1"
    local folder="$2"
    local dir="$mock_root/$folder"
    [[ -d "$dir" ]] || return 0

    echo "  Uploading $type ..."
    shopt -s nullglob
    for f in "$dir"/*.json; do
      local id
      id="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["id"])' "$f")"
      curl -fsS -X PUT "${base}/${type}/${id}" "${headers[@]}" --data-binary @"$f" >/dev/null
    done
    shopt -u nullglob
  }

  upload_folder "Practitioner"  "practitioners"
  upload_folder "Organization"  "organizations"
  upload_folder "Patient"       "patients"
  upload_folder "RelatedPerson" "relatedpersons"
  upload_folder "Immunization"  "immunizations"
  upload_folder "Location"      "locations"
  upload_folder "Encounter"     "encounters"

  echo "Seed complete."
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
until curl -fsS "${FHIR_BASE_URL}/metadata" >/dev/null; do
  sleep 5
done
echo "FHIR server is up."

# ---- 1b) Seed once (optional)
if $DO_SEED; then
  # Marker = one known mock resource
  marker_url="${FHIR_BASE_URL}/Patient/patient-noah"
  if [[ "$(http_code "$marker_url")" == "200" ]]; then
    echo "Seed skipped (marker exists: Patient/patient-noah)."
  else
    seed_fhir
  fi
fi

# ---- 2) Start backend (points to local FHIR)
echo "Starting backend..."
(
  cd "$PROJECT_ROOT/backend"
  mvn clean install
  FHIR_SERVER_BASE_URL="$FHIR_BASE_URL" ./mvnw spring-boot:run
) &
BACKEND_PID=$!

echo "Waiting for backend at ${BACKEND_URL} ..."
until curl -fsS "${BACKEND_URL}/v3/api-docs" >/dev/null; do
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
