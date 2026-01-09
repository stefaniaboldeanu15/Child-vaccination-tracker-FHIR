$FHIR_BASE = "http://localhost:8080/fhir"
$HEADERS = @{
  "Content-Type" = "application/fhir+json"
}

Write-Host "Uploading CodeSystems..."

Get-ChildItem "src/main/resources/codesystems/*.json" | ForEach-Object {
    $json = Get-Content $_.FullName -Raw
    $id = (ConvertFrom-Json $json).id
    Write-Host "  -> CodeSystem/$id"
    Invoke-RestMethod `
      -Uri "$FHIR_BASE/CodeSystem/$id" `
      -Method PUT `
      -Headers $HEADERS `
      -Body $json
}

Write-Host "Uploading ValueSets..."

Get-ChildItem "src/main/resources/valuesets/*.json" | ForEach-Object {
    $json = Get-Content $_.FullName -Raw
    $id = (ConvertFrom-Json $json).id
    Write-Host "  -> ValueSet/$id"
    Invoke-RestMethod `
      -Uri "$FHIR_BASE/ValueSet/$id" `
      -Method PUT `
      -Headers $HEADERS `
      -Body $json
}

Write-Host "DONE."
