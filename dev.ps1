# D:\MASTER\PRT - Vaccination Tracking FHIR - IntelliJ IDEA\Child-vaccination-tracker-FHIR\dev.ps1
param(
    [switch]$Install,
    [switch]$Seed,
    [string]$FhirBundlePath = "backend\seed\practitioner-view-bundle.json"
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backend = Join-Path $root "backend"
$frontend = Join-Path $root "frontend"
$bundlePath = Join-Path $root $FhirBundlePath

if (-not (Test-Path (Join-Path $backend "mvnw.cmd"))) {
    throw 'Missing "backend\mvnw.cmd"'
}

if (-not (Test-Path (Join-Path $frontend "package.json"))) {
    throw 'Missing "frontend\package.json"'
}

if (-not (Test-Path $bundlePath)) {
    throw "Missing bundle: $bundlePath"
}

function Start-NewTerminal {
    param(
        [Parameter(Mandatory = $true)][string]$WorkingDir,
        [Parameter(Mandatory = $true)][string]$Command
    )

    $safeDir = $WorkingDir.Replace("'", "''")
    $fullCommand = "Set-Location -LiteralPath '$safeDir'; $Command"

    Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-ExecutionPolicy", "Bypass",
        "-Command", $fullCommand
    ) | Out-Null
}

function Test-PortListening {
    param([int]$Port)

    $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    return $null -ne $connections
}

function Seed-FhirBundle {
    param([Parameter(Mandatory = $true)][string]$FilePath)

    if (-not (Test-Path $FilePath)) {
        throw "Seed bundle not found: $FilePath"
    }

    Write-Host "Uploading transaction bundle: $FilePath"

    try {
        $response = Invoke-RestMethod `
            -Uri "http://localhost:8080/fhir" `
            -Method Post `
            -ContentType "application/fhir+json" `
            -Body (Get-Content $FilePath -Raw)

        Write-Host "FHIR response:"
        $response | ConvertTo-Json -Depth 30
    }
    catch {
        Write-Host ""
        Write-Host "FHIR upload failed." -ForegroundColor Red

        if ($_.Exception.Response -and $_.Exception.Response.GetResponseStream()) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $body = $reader.ReadToEnd()
            $reader.Close()

            Write-Host "FHIR error body:" -ForegroundColor Yellow
            Write-Host $body
        } else {
            Write-Host $_.Exception.Message
        }

        throw
    }
}

function Verify-FhirSeed {
    Write-Host "Checking Practitioner dr.mueller..."
    $practitioner = Invoke-RestMethod `
        -Uri "http://localhost:8080/fhir/Practitioner?identifier=dr.mueller" `
        -Method Get

    $practitioner | ConvertTo-Json -Depth 20

    Write-Host "Checking Patient count..."
    $patients = Invoke-RestMethod `
        -Uri "http://localhost:8080/fhir/Patient" `
        -Method Get

    $patients | ConvertTo-Json -Depth 20
}

if ($Install) {
    Write-Host "Installing frontend dependencies..."
    Push-Location $frontend
    npm install
    Pop-Location
}

if (-not (Test-PortListening -Port 8080)) {
    throw "FHIR server is not listening on http://localhost:8080/fhir"
}

if (-not (Test-PortListening -Port 8081)) {
    Write-Host "Starting backend on port 8081..."
    Start-NewTerminal -WorkingDir $backend -Command ".\mvnw.cmd spring-boot:run"
} else {
    Write-Host "Backend already running on 8081."
}

if (-not (Test-PortListening -Port 5173)) {
    Write-Host "Starting frontend on port 5173..."
    Start-NewTerminal -WorkingDir $frontend -Command "npm run dev"
} else {
    Write-Host "Frontend already running on 5173."
}

if ($Seed) {
    Start-Sleep -Seconds 2
    Seed-FhirBundle -FilePath $bundlePath
    Start-Sleep -Seconds 1
    Verify-FhirSeed
}

Write-Host ""
Write-Host "FHIR server: http://localhost:8080/fhir"
Write-Host "Backend API: http://localhost:8081"
Write-Host "Frontend:    http://localhost:5173"
Write-Host "Bundle path: $bundlePath"
