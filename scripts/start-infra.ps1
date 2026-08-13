# Start Kafka + Mongo + Kafka UI + Swagger (MySQL stays local on :3306)
$ErrorActionPreference = "Stop"
$composeDir = Join-Path $PSScriptRoot "..\infra"
Set-Location $composeDir

Write-Host "Pulling & starting infra containers..."
docker compose pull
docker compose up -d

Write-Host ""
Write-Host "Services:"
Write-Host "  Swagger UI : http://localhost:8080"
Write-Host "  Kafka      : localhost:9092"
Write-Host "  Kafka UI   : http://localhost:8088"
Write-Host "  MongoDB    : localhost:27017 (root/password)"
Write-Host "  MySQL      : use your local instance on localhost:3306"
docker compose ps
