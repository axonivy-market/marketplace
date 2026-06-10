# AGENTS Guide for marketplace

This file gives AI coding agents the minimum repo-specific context to work safely and fast.

## Scope

- Workspace contains 3 main parts: `marketplace-ui`, `marketplace-service`, `marketplace-build`.
- Prefer module-level docs for details instead of duplicating them:
  - [README.md](README.md)
  - [marketplace-ui/README.md](marketplace-ui/README.md)
  - [marketplace-service/README.md](marketplace-service/README.md)
  - [marketplace-service/core/README.md](marketplace-service/core/README.md)
  - [marketplace-service/app/README.md](marketplace-service/app/README.md)
  - [marketplace-service/stable/README.md](marketplace-service/stable/README.md)

## Build, Run, Test

### UI (`marketplace-ui`)

- Install deps: `npm install`
- Dev server: `npm start` (Angular dev server)
- Build: `npm run build`
- Watch build: `npm run watch`
- Unit tests: `npm test`
- SSR serve (after build): `npm run serve:ssr:marketplace-ui`
- There is currently no `lint` script in `marketplace-ui/package.json`.

### Service (`marketplace-service`)

- Build all modules: `mvn clean install`
- Run app module (port 8080): `mvn -f app/pom.xml spring-boot:run`
- Run stable module (port 8085): `mvn -f stable/pom.xml spring-boot:run`
- Test all modules: `mvn test`
- Test single module: `mvn -f app/pom.xml test` (or `core` / `stable`)


## Architecture Boundaries

`marketplace-service` is a Maven multi-module project (Java 21, Spring Boot 3.2.5):

- `core`: shared domain/repository/service/util code for all service runtimes.
- `app`: main production backend for `market.axonivy.com` and primary traffic.
- `stable`: separate runtime for experimental/stable-targeted features.

Dependency direction:

- `app -> core`
- `stable -> core`

Design rule for changes:

- Structure by technical layer.
- Put reusable/shared logic in `core` first.
- Keep app-specific behavior in `app` or `stable`.

## Change Rules for Agents

- Do not edit generated/build outputs (for example `target/`, `dist/`) unless explicitly asked.
- Keep changes minimal and module-scoped.
- When changing shared contracts/entities in `core`, verify both `app` and `stable` compile paths.

## Validation Expectations

- Prefer targeted checks first, then broader checks if needed.
- Backend:
  - Targeted tests by module/class are preferred while iterating.
  - Run a broader `mvn test` before final handoff when feasible.
- Frontend:
  - Use `npm test` for unit validation.
  - Use `npm run build` when touching routing/build-sensitive code.

## Helpful Starting Points

- UI app structure: `marketplace-ui/src/app`
- Service main module code: `marketplace-service/app/src/main/java`
- Shared backend code: `marketplace-service/core/src/main/java`
- Stable backend code: `marketplace-service/stable/src/main/java`