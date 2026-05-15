# Marketplace Build

Infrastructure and deployment assets for Marketplace.

## Current Status

- Active: deployment template stack in `templates/` (nginx + ui + app + stable).
- Active: optional Matomo stack in `matomo/`.

## Folder Structure

```text
marketplace-build/
├── README.md
├── .dockerignore
├── templates/
│   ├── docker-compose.yml   # Release deployment only
│   ├── .env                 # Environment template
│   ├── Dockerfile           # NGINX image for deployment stack
│   └── dev/
│       └── docker-compose.yml   # Developer compose stack
├── nginx/
│   ├── nginx.conf           # Reverse proxy and cache rules
│   └── dev/
│       └── nginx.conf       # Developer NGINX config
├── matomo/
│   ├── docker-compose.yml   # Optional Matomo stack
│   ├── .env                 # Matomo credentials (local/dev)
│   ├── matomo.conf
│   └── config.ini.php

```

## Prerequisites

- Docker 24+
- Docker Compose v2 (`docker compose`)
- Existing Docker network: `marketplace-network`

Create networks if they do not exist yet:

```bash
docker network create marketplace-network || true
```

## Deploy Marketplace Stack

Use the correct compose file for your scenario:

- Release deployment: `templates/docker-compose.yml`
- Developer environment: `templates/dev/docker-compose.yml`

For developers, use `templates/dev/docker-compose.yml`.

1. Go to the template folder:

```bash
cd marketplace-build/templates/dev
```

2. Update environment values for your setup.

For developer compose, review values in `templates/dev/docker-compose.yml` and any referenced env files.
For release compose, update `templates/.env` values.

Important values to verify:

- `POSTGRES_HOST_URL`, `POSTGRES_USERNAME`, `POSTGRES_PASSWORD`
- `NGINX_PORT`
- `NGINX_CONFIG_PATH` should point to existing nginx config (recommended: `../nginx/nginx.conf`)

3. Ensure any host-mounted files/paths in your selected compose file exist on your machine.

For developers using `templates/dev/docker-compose.yml`, you can remove the bind mount for `/home/axonivy/marketplace/data/market-installations.json` if you do not need it locally.

4. Start the stack:

```bash
docker compose -f docker-compose.yml up -d --build
```

Alternative for developers: override environment values from command line when needed:

```bash
NGINX_PORT=8081 POSTGRES_HOST_URL=your-db-host docker compose -f docker-compose.yml up -d --build
```

Shell-provided variables take precedence over values from `.env`.

Cross-platform syntax:

Linux/macOS (bash/zsh):

```bash
NGINX_PORT=8081 POSTGRES_HOST_URL=your-db-host docker compose -f docker-compose.yml up -d --build
```

Windows Command Prompt (cmd.exe):

```cmd
set NGINX_PORT=8081&& set POSTGRES_HOST_URL=your-db-host&& docker compose -f docker-compose.yml up -d --build
```

Windows PowerShell:

```powershell
$env:NGINX_PORT="8081"; $env:POSTGRES_HOST_URL="your-db-host"; docker compose -f docker-compose.yml up -d --build
```

5. Stop the stack:

```bash
docker compose -f docker-compose.yml down
```

6. If your code changes are not reloading in development, remove Docker images to avoid stale cache, then rebuild:

```bash
docker compose -f docker-compose.yml down --rmi all
docker image prune -a -f
docker compose -f docker-compose.yml up -d --build
```

## Start Optional Matomo

```bash
cd marketplace-build/matomo
docker compose --env-file .env up -d
```

Notes:

- `matomo/.env` is intended for local/development usage.
- Matomo requires both external networks declared in its compose file.
- If you do not run Matomo in development, manually remove Matomo-related config from `nginx/dev/nginx.conf`.

## Image Release

Marketplace application images (`marketplace-ui`, `marketplace-app`, `marketplace-stable`) are pulled from GitHub Container Registry and tagged by `RELEASE_VERSION`.

Use your CI pipeline (for example, the repository Docker release workflow) to publish new tags, then update `RELEASE_VERSION` in `templates/.env` before redeploying.
