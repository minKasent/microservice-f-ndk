#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT_DIR="${1:-"${SCRIPT_DIR}/../keys"}"

if ! command -v openssl >/dev/null 2>&1; then
  echo "Error: openssl not found in PATH." >&2
  exit 1
fi

mkdir -p "${OUT_DIR}"

PRIVATE_KEY="${OUT_DIR}/jwt-private-key.pem"
PUBLIC_KEY="${OUT_DIR}/jwt-public-key.pem"

echo "Generating RSA 2048 private key -> ${PRIVATE_KEY}"
openssl genrsa -out "${PRIVATE_KEY}" 2048

echo "Extracting public key -> ${PUBLIC_KEY}"
openssl rsa -in "${PRIVATE_KEY}" -pubout -out "${PUBLIC_KEY}"

echo "Done."
echo "Private: ${PRIVATE_KEY}"
echo "Public : ${PUBLIC_KEY}"
