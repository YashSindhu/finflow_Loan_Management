param(
    [string]$Token = $env:SONAR_TOKEN,
    [string]$HostUrl = "http://sonarqube:9000",
    [string]$DockerNetwork = "finflow_finflow-network"
)

if (-not $Token) {
    Write-Host "Missing SonarQube token." -ForegroundColor Red
    Write-Host "Create a token in SonarQube, then run:" -ForegroundColor Yellow
    Write-Host '  $env:SONAR_TOKEN="your-token"; .\run-sonar.ps1' -ForegroundColor Yellow
    exit 1
}

Write-Host "Running backend tests and generating JaCoCo coverage..." -ForegroundColor Cyan
mvn clean test
if ($LASTEXITCODE -ne 0) {
    Write-Host "Tests failed. Fix tests before running SonarQube analysis." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "Running SonarQube analysis with Docker scanner..." -ForegroundColor Cyan
docker run --rm `
    --network $DockerNetwork `
    -e SONAR_HOST_URL=$HostUrl `
    -e SONAR_TOKEN=$Token `
    -v "${PWD}:/usr/src" `
    sonarsource/sonar-scanner-cli:latest
