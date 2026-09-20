#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_DIR="${SCRIPT_DIR}/../infra"

cd "${COMPOSE_DIR}"

echo "Pulling & starting infra containers..."
docker compose pull
docker compose up -d

echo ""
echo "Services:"
echo "  Swagger UI : http://localhost:8080"
echo "  Kafka      : localhost:9092"
echo "  Kafka UI   : http://localhost:8088"
echo "  MongoDB    : localhost:27017 (root/password)"
echo "  MySQL      : use your local instance on localhost:3306"
docker compose ps
