# Marketplace Architecture

## Summary

`marketplace` split into 3 main parts:

- `marketplace-ui`: Angular frontend
- `marketplace-service`: Spring Boot backend
- `marketplace-build`: runtime and build infra such as nginx, Matomo, Docker templates

Use this file as agent map:

- find main entrypoints fast
- know where feature code lives
- know what folders matter and what to ignore

## Top Tree

```text
marketplace/
├─ marketplace-ui/
├─ marketplace-service/
│  ├─ core/
│  ├─ app/
│  └─ stable/
└─ marketplace-build/
```

## marketplace-service

### `core`

Shared backend layer. Look here for reusable domain logic.

What to look for:

- shared config, constants, entities, enums
- repository contracts and shared service logic
- shared exceptions, models, and utilities

```text
core/
├─ src/main/java/com/axonivy/market/
│  ├─ config/         # shared backend config
│  ├─ constants/      # shared constant values
│  ├─ entity/        # shared persistence entities
│  ├─ enums/         # shared enum types
│  ├─ exceptions/    # shared exception types and handling
│  ├─ model/         # shared DTOs and projections
│  ├─ repository/    # persistence contracts and custom impls
│  ├─ service/       # shared business services
│  └─ util/          # shared helpers
├─ src/main/resources/ # core resources, if any
└─ src/test/java/com/axonivy/market/ # core tests
```

### `app`

Main production backend. Look here for runtime-specific code.

What to look for:

- web controllers and API endpoints
- app-specific services and repositories
- GitHub integration, scheduling, logging, and AOP
- app-specific resource files and database migrations

```text
app/
├─ src/main/java/com/axonivy/market/
│  ├─ aop/           # annotations and aspects
│  ├─ assembler/     # model assemblers
│  ├─ config/        # app runtime config
│  ├─ controller/    # HTTP endpoints
│  ├─ entity/        # app entities
│  ├─ exceptions/   # app exceptions and handlers
│  ├─ factory/      # object factories
│  ├─ github/       # GitHub integration
│  ├─ logging/      # log streaming and logging infra
│  ├─ model/        # app DTOs and view models
│  ├─ repository/   # repository layer
│  ├─ rest/         # external REST clients
│  ├─ schedulingtask/ # scheduled jobs
│  ├─ service/      # app business services
│  ├─ strategy/     # strategy implementations
│  └─ util/         # app helpers
├─ src/main/resources/
│  ├─ app-zip/      # packaged app ZIP metadata
│  ├─ db/migration/ # Flyway migrations
│  └─ github/       # GitHub related resource payloads
└─ src/test/java/com/axonivy/market/ # app tests
```

### `stable`

Separate backend runtime. Look here for a second Spring Boot module with its own packaging.

What to look for:

- module-specific startup and packaging
- stable/runtime-only code paths

```text
stable/
├─ src/
├─ pom.xml
├─ README.md
├─ Dockerfile
└─ .dockerignore
```

### Ignore in service tree

These exist in checkout but are not architecture signal:

- `data/`
- `logs/`
- `target/`
- `.idea/`

## marketplace-ui

Angular frontend. Look here for user-facing app structure.

What to look for:

- app shell and routing
- feature modules
- shared UI components and utilities
- environment config, assets, and SSR entrypoints

```text
marketplace-ui/
├─ src/
│  ├─ app/
│  │  ├─ core/        # app-wide infrastructure
│  │  ├─ modules/     # feature areas
│  │  ├─ shared/      # reusable UI and helpers
│  │  ├─ types/       # global TS types
│  │  ├─ app.component.*
│  │  ├─ app.config.*
│  │  └─ app.routes.ts
│  ├─ assets/         # fonts, images, i18n, styles, team assets
│  ├─ environments/   # environment configs
│  ├─ main.ts         # browser entry
│  ├─ main.server.ts  # SSR entry
│  └─ styles.scss     # global styles
├─ angular.json
├─ package.json
├─ server.ts
├─ tsconfig*.json
└─ test-setup.ts
```

### `core`

App-wide infra. Look here when code affects many pages or app bootstrapping.

- `configs`
- `interceptors`
- `models`
- `resolver`
- `services`

### `modules`

Feature areas. Look here for page-level code and route-owned behavior.

- `admin-dashboard`
- `home`
- `monitor`
- `news`
- `product`
- `release-preview`

### `shared`

Reusable UI and helpers. Look here for code reused by multiple modules.

- `components`
- `constants`
- `enums`
- `mocks`
- `models`
- `pipes`
- `services`
- `utils`

### Ignore in UI tree

These exist in checkout but are not architecture signal:

- `node_modules/`
- `dist/`
- `.angular/`
- `.vscode/`

## Read First

If agent need fast context, start here:

- Backend entry: `marketplace-service/app/src/main/java/com/axonivy/market/MarketplaceServiceApplication.java`
- Backend web API: `marketplace-service/app/src/main/java/com/axonivy/market/controller/`
- Backend shared logic: `marketplace-service/core/src/main/java/com/axonivy/market/`
- Frontend entry: `marketplace-ui/src/app/app.routes.ts`
- Frontend module map: `marketplace-ui/src/app/modules/`
- Frontend shared layer: `marketplace-ui/src/app/shared/`

## Boundaries

- Shared backend logic live in `core`
- App-specific backend logic stay in `app`
- Frontend shared UI/util logic stay in `shared`
- Feature code stay in `modules`

## Notes

- Keep `ARCHITECTURE.md` short and agent-first.
- Update this doc when new module or major runtime boundary appears.
