# Windows PowerShell equivalent of Chapter 11 gen-rsa-key.sh
# Requires OpenSSL on PATH (Git for Windows includes openssl.exe)

param(
    [string]$OutDir = (Join-Path $PSScriptRoot "..\keys")
)

$ErrorActionPreference = "Stop"

if (-not (Get-Command openssl -ErrorAction SilentlyContinue)) {
    Write-Error "openssl not found. Install Git for Windows or OpenSSL and add it to PATH."
}

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

$privateKey = Join-Path $OutDir "jwt-private-key.pem"
$publicKey = Join-Path $OutDir "jwt-public-key.pem"

Write-Host "Generating RSA 2048 private key -> $privateKey"
openssl genrsa -out $privateKey 2048
if ($LASTEXITCODE -ne 0) { throw "openssl genrsa failed" }

Write-Host "Extracting public key -> $publicKey"
openssl rsa -in $privateKey -pubout -out $publicKey
if ($LASTEXITCODE -ne 0) { throw "openssl rsa failed" }

Write-Host "Done."
Write-Host "Private: $privateKey"
Write-Host "Public : $publicKey"
