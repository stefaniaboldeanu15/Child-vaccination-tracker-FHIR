$FHIR_BASE = "http://localhost:8080/fhir"
$HEADERS = @{ "Content-Type" = "application/fhir+json" }

function Upload($type, $folder) {
  Write-Host "Uploading $type..."
  Get-ChildItem "src/main/resources/_mock_data/$folder/*.json" | ForEach-Object {
    $json = Get-Content $_ -Raw | ConvertFrom-Json
    $id = $json.id
    Invoke-RestMethod `
      -Method PUT `
      -Uri "$FHIR_BASE/$type/$id" `
      -Headers $HEADERS `
      -Body (Get-Content $_ -Raw)
  }
}
Upload "Practitioner" "practitioners"
Upload "Organization" "organizations"
Upload "Patient" "patients"
Upload "RelatedPerson" "relatedpersons"
Upload "Immunization" "immunizations"
Upload "Location" "locations"
Upload "Encounter" "encounters"


Write-Host "uploaded successfully"
