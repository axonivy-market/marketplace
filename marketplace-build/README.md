# Marketplace Build

Infrastructure and deployment assets for Marketplace.

## Current Status

- Active: deployment template stack in `templates/` (nginx + ui + app + stable).
- Active: optional Matomo stack in `matomo/`.
- Active: optional local SonarQube stack in `sonar/`.

## Folder Structure

```text
marketplace-build/
├── README.md
├── .dockerignore
├── templates/
│   ├── docker-compose.yml   # Main deployment template
│   ├── .env                 # Environment template
│   └── Dockerfile           # NGINX image for deployment stack
├── nginx/
│   └── nginx.conf           # Reverse proxy and cache rules
├── matomo/
│   ├── docker-compose.yml   # Optional Matomo stack
│   ├── .env                 # Matomo credentials (local/dev)
│   ├── matomo.conf
│   └── config.ini.php
└── sonar/
    ├── docker-compose.yml   # Optional local SonarQube stack
    ├── init.sh
    └── init-db.sh
```

## Prerequisites

- Docker 24+
- Docker Compose v2 (`docker compose`)
- Existing Docker network: `marketplace-network`
- For Matomo: additional Docker network `market-network`

Create networks if they do not exist yet:

```bash
docker network create marketplace-network || true
docker network create market-network || true
```

## Deploy Marketplace Stack (Template)

The main stack is defined in `templates/docker-compose.yml`.

1. Go to the template folder:

```bash
cd marketplace-build/templates
```

2. Update `templates/.env` values for your environment.

Important values to verify:

- `RELEASE_VERSION` (image tag for `ghcr.io/axonivy-market/*` images)
- `POSTGRES_HOST_URL`, `POSTGRES_USERNAME`, `POSTGRES_PASSWORD`
- `NGINX_PORT`
- `NGINX_CONFIG_PATH` should point to existing nginx config (recommended: `../nginx/nginx.conf`)

3. Ensure any host-mounted files/paths in `templates/docker-compose.yml` exist on your machine (for example `/home/axonivy/marketplace/data/market-installations.json`).

4. Start the stack:

```bash
docker compose up -d --build
```

5. Stop the stack:

```bash
docker compose down
```

## Start Optional Matomo

```bash
cd marketplace-build/matomo
docker compose --env-file .env up -d
```

Notes:

- `matomo/.env` is intended for local/development usage.
- Matomo requires both external networks declared in its compose file.

## Start Optional SonarQube (Local)

```bash
cd marketplace-build/sonar
docker compose up -d
```

Notes:

- This stack exposes SonarQube on `http://localhost:9000`.
- It also binds PostgreSQL to `5432`, which may conflict with another local PostgreSQL.

## Image Release

Marketplace application images (`marketplace-ui`, `marketplace-app`, `marketplace-stable`) are pulled from GitHub Container Registry and tagged by `RELEASE_VERSION`.

Use your CI pipeline (for example, the repository Docker release workflow) to publish new tags, then update `RELEASE_VERSION` in `templates/.env` before redeploying.
