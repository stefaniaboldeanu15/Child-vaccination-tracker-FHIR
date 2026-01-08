$FHIR_BASE = "http://localhost:8080/fhir"
$HEADERS = @{ "Content-Type" = "application/fhir+json" }

Write-Host "Uploading StructureDefinitions..."

Get-ChildItem "src/main/resources/structuredefinition/*.json" | ForEach-Object {
    $json = Get-Content $_ -Raw | ConvertFrom-Json
    $id = $json.id

    Write-Host " → Uploading profile $id"

    Invoke-RestMethod `
        -Method PUT `
        -Uri "$FHIR_BASE/StructureDefinition/$id" `
        -Headers $HEADERS `
        -Body (Get-Content $_ -Raw)
}

Write-Host "StructureDefinitions uploaded"
