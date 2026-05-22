# Start SolarIQ backend with in-memory H2 (no PostgreSQL).
$ErrorActionPreference = "Stop"
$backendRoot = $PSScriptRoot
$mavenHome = Join-Path $backendRoot ".tools\apache-maven-3.9.16"
$mvn = Join-Path $mavenHome "bin\mvn.cmd"

if (-not (Test-Path $mvn)) {
    Write-Host "Downloading Maven (one-time)..."
    $toolsDir = Join-Path $backendRoot ".tools"
    New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null
    $zip = Join-Path $toolsDir "maven.zip"
    Invoke-WebRequest -Uri "https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip" -OutFile $zip
    Expand-Archive -Path $zip -DestinationPath $toolsDir -Force
    Remove-Item $zip
}

Set-Location $backendRoot
& $mvn spring-boot:run "-Dspring-boot.run.profiles=local"
