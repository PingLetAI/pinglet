#!/bin/bash
DOCKER_COMPOSE_BIN="/usr/bin/docker compose"
COMPOSE_FILE="docker-compose.prod.yml"

echo "Pulling latest changes..."
git pull origin main

echo "Syncing root .env for Docker Compose variable substitution..."
grep -E '^(POSTGRES_DB|POSTGRES_USER|POSTGRES_PASSWORD)=' backend/.env.production > .env

echo "Rebuilding backend..."
$DOCKER_COMPOSE_BIN -f $COMPOSE_FILE build backend worker

echo "Running migrations..."
$DOCKER_COMPOSE_BIN -f $COMPOSE_FILE exec -T backend npx prisma migrate deploy

echo "Restarting services..."
$DOCKER_COMPOSE_BIN -f $COMPOSE_FILE up -d --force-recreate backend worker

echo ""
$DOCKER_COMPOSE_BIN -f $COMPOSE_FILE logs --tail=20 backend worker
