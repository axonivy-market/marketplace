# Marketplace Build

Infrastructure and deployment assets for Marketplace services.

## Repository Layout

```text
marketplace-build/
├── README.md
├── .dockerignore
├── templates/
│   ├── .env
│   ├── docker-compose.yml
│   └── dev/
│       ├── dev.env
│       └── docker-compose.yml
├── nginx/
│   ├── .env
│   ├── Dockerfile
│   ├── docker-compose.yml
│   ├── dev/
│   │   └── nginx.conf
│   ├── preview/
│   │   └── nginx.conf
│   └── prod/
│       ├── nginx-node-1.conf
│       └── nginx-node-2.conf
└── matomo/
    ├── .env
    ├── config.ini.php
    ├── docker-compose.yml
    └── matomo.conf
```

## What Each Stack Does

- `templates/docker-compose.yml`: application services using released images (`ui`, `app`, `stable`).
- `templates/dev/docker-compose.yml`: local development build of application services.
- `nginx/docker-compose.yml`: reverse proxy service (`nginx`) with cache/browser shared volumes.
- `matomo/docker-compose.yml`: optional Matomo stack (`matomo-db`, `matomo-app`, `matomo-nginx`).

## Prerequisites

- Docker 24+
- Docker Compose v2 (`docker compose`)
- External Docker networks:
  - `marketplace-network`
  - `market-network` (only required for Matomo)
- External Docker volumes used by app/nginx stacks:
  - `marketplace_marketcache`
  - `marketplace_marketbrowser`

Create them if needed:

```bash
docker network create marketplace-network || true
docker network create market-network || true

docker volume create marketplace_marketcache || true
docker volume create marketplace_marketbrowser || true
```

## Environment Files

- `templates/.env`: shared app/service variables (`RELEASE_VERSION`, DB settings, app logs, API URLs, etc.).
- `templates/dev/dev.env`: for local only, to share app/service variables (`BUILD_ENV`, `BUILD_VERSION`.).
- `nginx/.env`: nginx container settings (`NGINX_VERSION`, `NGINX_CONFIG_PATH`, `NGINX_PORT`, log/cache paths).
- `matomo/.env`: local/dev Matomo database credentials.

Important: make sure all host paths referenced in env files exist before starting containers.

## Start Application Services

Release images:

```bash
cd marketplace-build/templates
docker compose up -d
```

Local development build:
Replace `/home/axonivy/market/logs/` to your local path

```bash
cd marketplace-build/templates/dev
mvn -f ../../../marketplace-service/pom.xml clean install -DskipTests=true
export STABLE_LOG_PATH=/home/axonivy/market/logs/stable
export APP_LOG_PATH=/home/axonivy/market/logs/app
docker compose up -d --build
```

Stop application services:

```bash
docker compose down --rmi all -v
```

## Start NGINX Gateway

Choose one config by setting `NGINX_CONFIG_PATH` in `nginx/.env`:

- Development: `./dev/nginx.conf`
- Preview: `./preview/nginx.conf`
- Production node 1: `./prod/nginx-node-1.conf`
- Production node 2: `./prod/nginx-node-2.conf`

Then run:

```bash
cd marketplace-build/nginx
docker compose up -d --build
```

Stop nginx:

```bash
docker compose down --rmi all -v
```

## Start Matomo

For local environment, we recommended to commmend out in the `matomo` stuff in `./dev/nginx.conf` and skip this step. But if you want a full setup, let start it.

```bash
cd marketplace-build/matomo
docker compose up -d
```

Stop Matomo:

```bash
docker compose down --rmi all -v
```

## Typical Local Startup Order

1. Start app services (`templates/dev`).
2. Start nginx (`nginx`) and point `NGINX_CONFIG_PATH` to `./dev/nginx.conf`.
3. Start Matomo only if needed.

## Release Notes

- `templates/docker-compose.yml` pulls release images from GitHub Container Registry using `RELEASE_VERSION`.
- Update `RELEASE_VERSION` in `templates/.env` when rolling out a new version.
