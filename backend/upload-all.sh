#!/usr/bin/env bash
set -euo pipefail

# Use the SAME FHIR server your backend uses.
# If backend uses public HAPI (default in your application.properties), keep this:
FHIR_BASE="${FHIR_BASE:-https://hapi.fhir.org/baseR5}"

# If you run a local HAPI server instead, use:
# FHIR_BASE="${FHIR_BASE:-http://localhost:8080/fhir}"

HEADERS=(-H "Content-Type: application/fhir+json")

upload_dir_by_type() {
  local type="$1"
  local dir="$2"
  echo "Uploading ${type} from ${dir}..."
  shopt -s nullglob
  for f in "$dir"/*.json; do
    local id
    id="$(jq -r '.id' "$f")"
    if [[ -z "$id" || "$id" == "null" ]]; then
      echo "Skipping $f (missing .id)"
      continue
    fi
    echo "  -> ${type}/${id}"
    curl -fsS -X PUT "${FHIR_BASE}/${type}/${id}" "${HEADERS[@]}" --data-binary @"$f" >/dev/null
  done
  shopt -u nullglob
}

echo "FHIR FULL SETUP STARTING"
echo "FHIR_BASE=${FHIR_BASE}"

# Mock data
upload_dir_by_type "Practitioner" "src/main/resources/_mock_data/practitioners"
upload_dir_by_type "Organization" "src/main/resources/_mock_data/organizations"
upload_dir_by_type "Patient" "src/main/resources/_mock_data/patients"
upload_dir_by_type "RelatedPerson" "src/main/resources/_mock_data/relatedpersons"
upload_dir_by_type "Immunization" "src/main/resources/_mock_data/immunizations"
upload_dir_by_type "Location" "src/main/resources/_mock_data/locations"
upload_dir_by_type "Encounter" "src/main/resources/_mock_data/encounters"

echo "FHIR SETUP COMPLETED SUCCESSFULLY"
