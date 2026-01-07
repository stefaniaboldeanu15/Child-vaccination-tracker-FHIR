$FHIR_BASE = "http://localhost:8080/fhir"

Write-Host "Uploading Practitioners..."
Get-ChildItem "src/main/resources/_mock_data/practitioners/*.json" | ForEach-Object {
    Invoke-RestMethod `
        -Method Post `
        -Uri "$FHIR_BASE/Practitioner" `
        -ContentType "application/fhir+json" `
        -Body (Get-Content $_.FullName -Raw)
}

Write-Host "Uploading Organizations..."
Get-ChildItem "src/main/resources/_mock_data/organizations/*.json" | ForEach-Object {
    Invoke-RestMethod `
        -Method Post `
        -Uri "$FHIR_BASE/Organization" `
        -ContentType "application/fhir+json" `
        -Body (Get-Content $_.FullName -Raw)
}

Write-Host "Uploading Locations..."
Get-ChildItem "src/main/resources/_mock_data/locations/*.json" | ForEach-Object {
    Invoke-RestMethod `
        -Method Post `
        -Uri "$FHIR_BASE/Location" `
        -ContentType "application/fhir+json" `
        -Body (Get-Content $_.FullName -Raw)
}

Write-Host "Uploading Patients..."
Get-ChildItem "src/main/resources/_mock_data/patients/*.json" | ForEach-Object {
    Invoke-RestMethod `
        -Method Post `
        -Uri "$FHIR_BASE/Patient" `
        -ContentType "application/fhir+json" `
        -Body (Get-Content $_.FullName -Raw)
}

Write-Host "Uploading Related Persons..."
Get-ChildItem "src/main/resources/_mock_data/relatedpersons/*.json" | ForEach-Object {
    Invoke-RestMethod `
        -Method Post `
        -Uri "$FHIR_BASE/RelatedPerson" `
        -ContentType "application/fhir+json" `
        -Body (Get-Content $_.FullName -Raw)
}

Write-Host "Uploading Encounters..."
Get-ChildItem "src/main/resources/_mock_data/encounters/*.json" | ForEach-Object {
    Invoke-RestMethod `
        -Method Post `
        -Uri "$FHIR_BASE/Encounter" `
        -ContentType "application/fhir+json" `
        -Body (Get-Content $_.FullName -Raw)
}

Write-Host "Uploading Immunizations..."
Get-ChildItem "src/main/resources/_mock_data/immunizations/*.json" | ForEach-Object {
    Invoke-RestMethod `
        -Method Post `
        -Uri "$FHIR_BASE/Immunization" `
        -ContentType "application/fhir+json" `
        -Body (Get-Content $_.FullName -Raw)
}

Write-Host "Mock data upload completed."
