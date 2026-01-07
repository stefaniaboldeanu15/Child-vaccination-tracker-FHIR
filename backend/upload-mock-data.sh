#!/bin/bash

FHIR_BASE="http://localhost:8080/fhir"

echo "Uploading Practitioners..."
for file in src/main/resources/_mock_data/practitioners/*.json; do
  curl -X POST "$FHIR_BASE/Practitioner" \
    -H "Content-Type: application/fhir+json" \
   -d @"$file"
done

echo "Uploading Organizations..."
for file in src/main/resources/_mock_data/organizations/*.json; do
  curl -X POST "$FHIR_BASE/Organization" \
    -H "Content-Type: application/fhir+json" \
    -d @"$file"
done

echo "Uploading Locations..."
for file in src/main/resources/_mock_data/locations/*.json; do
  curl -X POST "$FHIR_BASE/Location" \
    -H "Content-Type: application/fhir+json" \
    -d @"$file"
done

echo "Uploading Patients..."
for file in src/main/resources/_mock_data/patients/*.json; do
  curl -X POST "$FHIR_BASE/Patient" \
    -H "Content-Type: application/fhir+json" \
    -d @"$file"
done

echo "Uploading Related Persons..."
for file in src/main/resources/_mock_data/relatedpersons/*.json; do
  curl -X POST "$FHIR_BASE/RelatedPerson" \
    -H "Content-Type: application/fhir+json" \
    -d @"$file"
done

echo "Uploading Encounters..."
for file in src/main/resources/_mock_data/encounters/*.json; do
  curl -X POST "$FHIR_BASE/Encounter" \
    -H "Content-Type: application/fhir+json" \
    -d @"$file"
done

echo "Uploading Immunizations..."
for file in src/main/resources/_mock_data/immunizations/*.json; do
  curl -X POST "$FHIR_BASE/Immunization" \
    -H "Content-Type: application/fhir+json" \
    -d @"$file"
done




echo "Mock data upload completed."
