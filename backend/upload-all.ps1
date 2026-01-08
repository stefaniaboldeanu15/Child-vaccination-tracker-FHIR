
Write-Host "FHIR FULL SETUP STARTING"

Write-Host "`Uploading Terminology..."
& "$PSScriptRoot/upload-terminology.ps1"

Write-Host "`Uploading Profiles..."
& "$PSScriptRoot/upload-profiles.ps1"

Write-Host "`Uploading Mock Data..."
& "$PSScriptRoot/upload-mock-data.ps1"

Write-Host "FHIR SETUP COMPLETED SUCCESSFULLY"

