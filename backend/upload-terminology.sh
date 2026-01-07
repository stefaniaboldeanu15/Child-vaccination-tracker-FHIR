#!/bin/bash

# ===============================
# Configuration
# ===============================
FHIR_BASE_URL="http://localhost:8080/fhir"
HEADERS="-H Content-Type:application/fhir+json"

echo "Uploading CodeSystems..."
for file in src/main/resources/codesystems/*.json; do
  echo "→ $file"
  curl -X POST $FHIR_BASE_URL/CodeSystem \
       $HEADERS \
       --data-binary @"$file"
  echo ""
done

echo "Uploading ValueSets..."
for file in src/main/resources/valuesets/*.json; do
  echo "→ $file"
  curl -X POST $FHIR_BASE_URL/ValueSet \
       $HEADERS \
       --data-binary @"$file"
  echo ""
done

echo "Uploading Profiles (StructureDefinitions)..."
for file in src/main/resources/profiles/*.json; do
  echo "→ $file"
  curl -X POST $FHIR_BASE_URL/StructureDefinition \
       $HEADERS \
       --data-binary @"$file"
  echo ""
done

echo "Terminology & profiles upload finished"
