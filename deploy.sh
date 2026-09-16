#!/bin/bash
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"
COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE="backend/.env.production"

compose() {
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

test -f "$ENV_FILE" || { echo "Missing $ENV_FILE" >&2; exit 1; }

echo "Pulling latest changes..."
git pull --ff-only origin main

echo "Rebuilding backend..."
compose build backend worker

echo "Stopping API and draining the old worker before switching versions..."
compose stop --timeout 300 backend worker

echo "Running migrations..."
# Run from the newly built image, not the previous release's container.
compose run --rm --no-deps backend npx prisma migrate deploy

echo "Restarting services..."
compose up -d --no-deps --force-recreate --wait --wait-timeout 180 backend worker

echo ""
compose ps
compose logs --tail=20 backend worker
