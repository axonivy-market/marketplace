# .build
## Jenkins pipeline

The `.build` folder contains Jenkins pipelines and shell scripts used to:

- build and package Marketplace services/UI
- publish Docker images to GHCR
- prepare release configuration on target nodes
- deploy and verify release health
- promote or roll back releases
- deploy and switch nginx configurations per environment

This folder is infrastructure automation code. It is not application runtime code.

## Folder Structure

```text
.build/
  deploy/
    internal-docker/Jenkinsfile   # develop branch build/deploy to internal docker host
  nginx/
    Jenkinsfile                   # nginx release pipeline
  pipeline-lib/
    deployConfig.groovy            # shared node lookup + production approval (4-eyes)
    deploy_run_nginx_on_nodes.groovy      # shared per-node ssh/script execution for nginx pipeline
  release/
    Jenkinsfile                   # full release pipeline: versioning, image publish, deploy
  scripts/
    release/                      # release rollout scripts (config, deploy, health, promote)
    nginx/                        # nginx prepare/deploy scripts
    utils/                        # utility scripts (GHCR retention cleanup)
```

## Workflow

### 1) Full Release Pipeline

Entry point: `.build/release/Jenkinsfile`

Typical flow:

1. Checkout source and compute `RELEASE_VERSION` from Maven snapshot.
2. Update Maven + UI package versions, create/push Git tag.
3. Build artifacts and Docker images.
4. Push images to GHCR (`marketplace-ui`, `marketplace-app`, `marketplace-stable`).
5. Clean old GHCR versions via `.build/scripts/utils/ghcr-cleanup.sh`.
6. Deploy sequentially to `PREVIEW`, then `PROD_1`, then `PROD_2`.
7. Use `prepare-release-config.sh` + `run-release-rollout.sh` per node.

Important environment/credentials used by pipeline include:

- `MARKETPLACE_GIT_URL`, `GH_TOKEN`, `GITHUB_ACTOR`
- `PREVIEW_ENV_FILE` (template `.env` seed)
- `ghcr-io-token` (pull auth on nodes)
- node credentials + SSH key + per-node secret env file

### 2) Internal Dev Docker Deployment

Entry point: `.build/deploy/internal-docker/Jenkinsfile`

Behavior:

- polls SCM on `develop`
- builds Maven modules with tests skipped
- runs compose in `marketplace-build/templates/dev`
- restarts local nginx containers with prefix `market-dev-nginx-`

### 3) Nginx Deployment Pipeline

Entry point: `.build/nginx/Jenkinsfile`

Parameters:

- `BRANCH`: source branch for nginx config
- `DEPLOYMENT_TARGET`: `DEV`, `PREVIEW`, `PROD_1`, `PROD_2`
- `NGINX_VERSION`: release folder/version (default `latest`)

Behavior:

- resolves target nodes via `pipeline-lib/deployConfig.groovy`
- requires approval for production targets
- prepares nginx assets on remote node
- performs green validation, cutover, and current symlink switch

## Technical Notes

### Release Script Chain

Main orchestrators:

- `.build/scripts/release/prepare-release-config.sh`
  - uploads templates/secrets to remote temp dir
  - creates release workspace paths under `/home/axonivy/marketplace/releases/<version>`
  - merges `.env` from current/template/secret with precedence and enforced keys:
    - `RELEASE_VERSION`
    - `MARKET_NODE_NUMBER`
- `.build/scripts/release/run-release-rollout.sh`
  - uploads rollout scripts + temporary GHCR credentials to remote host
  - executes rollout steps in order:
    1. `step1-deploy-release.sh`
    2. `step2-verify-release-health.sh`
    3. `step3-promote-release.sh`

Step details:

- `step1-deploy-release.sh`
  - logs in to GHCR
  - starts new release compose stack using versioned compose project names
  - keeps old release running until health check + promotion pass
- `step2-verify-release-health.sh`
  - validates health target syntax: `{port}/{app-name}`
  - checks `/<app-name>/actuator/health` from container network namespace
  - on failure: rolls back by stopping new release and restarting old release
- `step3-promote-release.sh`
  - switches `current` symlink to new release
  - reloads nginx (container-first, host fallback)
  - stops old release and prunes old images

Shared logic:

- `.build/scripts/release/release-context-lib.sh`
  - compose naming normalization
  - old/new release context resolution
  - nginx reload helper
  - GHCR login helper

### Nginx Script Chain

- `.build/scripts/nginx/prepare__configs-and-docker-files.sh`
  - copies nginx config, compose file, Dockerfile, and generated `.env` to target release path
  - path base is environment-aware:
    - prod: `/home/axonivy/marketplace/nginx/<version>`
    - non-prod: `/home/axonivy/marketplace/nginx/<env>/<version>`

- `.build/scripts/nginx/deploy__new-version-and-switch-release.sh`
  - computes standardized compose project names
  - builds image and runs a temporary green container for health validation
  - if healthy: stops old release, starts new release, ensures external network attachment, updates `current` symlink
  - if failed: best-effort rollback to previous nginx release

### Related Utility Script

- `.build/scripts/utils/ghcr-cleanup.sh`
  - queries GHCR package versions via GitHub API
  - keeps latest `VERSION_RETENTION_COUNT` versions
  - deletes older versions
  - requires: `jq`, `GH_TOKEN`, `IMAGE_NAME`, `GITHUB_REPOSITORY_OWNER`

### Operational Conventions

- Remote base path: `/home/axonivy/marketplace`
- Release symlink: `/home/axonivy/marketplace/releases/current`
- Strict shell mode is enabled in scripts (`set -euo pipefail` or `set -Eeuo pipefail`).
- Most deploy scripts accept SSH overrides:
  - `SSH_REMOTE_USER`
  - `SSH_PRIVATE_KEY_FILE`

### Safe Usage Tips

- Run production deployments only through Jenkins jobs with configured approvals.
- Ensure health targets are correct (`port/service`) before rollout.
- Keep `deployment-target` and approver managed files up to date.
- Validate required credentials exist before triggering pipelines.