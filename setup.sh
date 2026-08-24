#!/bin/bash
SERVICE_NAME="pinglet"
APP_DIR="/home/tinkerpal/pinglet"
REPO_URL="git@github.com:your-org/pinglet.git"
DOCKER_COMPOSE_BIN="/usr/bin/docker compose"
SERVICE_FILE="/etc/systemd/system/${SERVICE_NAME}.service"

if [ ! -d "$APP_DIR" ]; then
  echo "Error: App directory $APP_DIR does not exist."
  exit 1
fi

if [ -z "$DOCKER_COMPOSE_BIN" ]; then
  echo "Error: Docker Compose not found. Is Docker installed?"
  exit 1
fi

echo "Cleaning up unused Docker resources..."
docker system prune -f

echo "Pulling latest changes..."
cd "$APP_DIR"
git pull origin main || echo "Git pull failed or not a git repo, continuing..."

if [ ! -f "$APP_DIR/backend/.env.production" ]; then
  echo "Creating backend/.env.production from template..."
  cp "$APP_DIR/backend/.env.production.example" "$APP_DIR/backend/.env.production"

  JWT_SECRET=$(openssl rand -hex 32)
  ADMIN_SECRET=$(openssl rand -hex 32)
  OTP_SECRET=$(openssl rand -hex 32)
  PG_PASSWORD=$(openssl rand -base64 24 | tr -dc 'a-zA-Z0-9' | head -c 32)

  sed -i.bak \
    -e "s|CHANGE_ME_GENERATE_WITH_OPENSSL|$JWT_SECRET|g" \
    -e "s|CHANGE_ME_TO_A_STRONG_PASSWORD|$PG_PASSWORD|g" \
    "$APP_DIR/backend/.env.production"
  sed -i "s|ADMIN_SECRET=.*|ADMIN_SECRET=$ADMIN_SECRET|" "$APP_DIR/backend/.env.production"
  sed -i "s|OTP_SECRET=.*|OTP_SECRET=$OTP_SECRET|" "$APP_DIR/backend/.env.production"
  rm -f "$APP_DIR/backend/.env.production.bak"

  echo ""
  echo "*** You MUST edit $APP_DIR/backend/.env.production and fill in: ***"
  echo "    - OPENAI_API_KEY"
  echo "    - RESEND_API_KEY"
  echo "    - GOOGLE_PLAY_SERVICE_ACCOUNT_JSON"
  echo ""
fi

echo "Syncing root .env for Docker Compose variable substitution..."
grep -E '^(POSTGRES_DB|POSTGRES_USER|POSTGRES_PASSWORD)=' "$APP_DIR/backend/.env.production" > "$APP_DIR/.env"

echo "Building Docker images..."
$DOCKER_COMPOSE_BIN -f docker-compose.prod.yml build

echo "Running database migrations..."
$DOCKER_COMPOSE_BIN -f docker-compose.prod.yml exec -T backend npx prisma migrate deploy || true

echo "Seeding database (first deploy only)..."
$DOCKER_COMPOSE_BIN -f docker-compose.prod.yml exec -T backend npm run seed || true

echo "Creating systemd service file at $SERVICE_FILE..."

sudo tee "$SERVICE_FILE" > /dev/null <<EOF
[Unit]
Description=Docker Compose App - $SERVICE_NAME
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=true
WorkingDirectory=$APP_DIR
ExecStart=$DOCKER_COMPOSE_BIN -f docker-compose.prod.yml up -d
ExecStop=$DOCKER_COMPOSE_BIN -f docker-compose.prod.yml down
TimeoutStartSec=0

[Install]
WantedBy=multi-user.target
EOF

echo "Reloading systemd and enabling service..."
sudo systemctl daemon-reexec
sudo systemctl daemon-reload
sudo systemctl enable ${SERVICE_NAME}.service

echo "Systemd service '$SERVICE_NAME' has been created and enabled."
read -p "Do you want to start the app now? (y/n): " choice

if [[ "$choice" =~ ^[Yy]$ ]]; then
  sudo systemctl start ${SERVICE_NAME}.service
  echo "Service started. Run: sudo systemctl status ${SERVICE_NAME}.service"
else
  echo "Start manually with: sudo systemctl start ${SERVICE_NAME}.service"
fi
