# AGENTS.md

## Bottom Line

- `marketplace-service` is a 3-module Spring Boot 4.1.0 Maven project:
  - `core`: shared entities, repositories, base services, utils
  - `app`: main CRUD/admin API with GitHub integration
  - `stable`: read-only API for public/stable consumers
- Prefer `core` first when a change should affect both runtimes.

## Project Overview

- Tech stack:
  - Java 25
  - Spring Boot 4.1.0
  - Spring Data JPA
  - PostgreSQL
  - Flyway in `app`
  - Springdoc OpenAPI
  - Lombok
- Architecture:
  - parent Maven aggregator at `marketplace-service/pom.xml`
  - `app -> core`
  - `stable -> core`
  - `app` and `stable` do not depend on each other
- Runtime entrypoints:
  - `app/src/main/java/com/axonivy/market/MarketplaceServiceApplication.java`
  - `stable/src/main/java/com/axonivy/market/stable/MarketplaceStableApplication.java`

## Project Structure

```text
marketplace-service/
|-- pom.xml                         # parent Maven aggregator
|-- README.md                       # human-readable module overview
|-- AGENTS.md                       # agent rules and navigation hints
|-- core/                           # shared foundation used by both runtimes
|   |-- pom.xml                     # shared library module
|   `-- src/
|       |-- main/java/com/axonivy/market/core/   # shared code; check here first for cross-module behavior
|       |   |-- config/                          # base Spring, web, and OpenAPI configuration
|       |   |-- constants/                       # package names, routes, regex, shared constants
|       |   |-- controller/                      # reusable base controllers
|       |   |-- entity/                          # shared JPA entities and composite keys
|       |   |-- repository/                      # shared repository contracts and custom implementations
|       |   |-- service/                         # shared service interfaces
|       |   |-- service/impl/                    # core product, version, and image logic
|       |   `-- utils/                           # helpers such as version and Maven parsing
|       `-- test/                                # shared tests; app uses core test-jar too
|-- app/                            # main CRUD/admin API and sync runtime
|   |-- pom.xml                     # app module with Flyway and GitHub integration
|   |-- Dockerfile                  # container build for app runtime
|   `-- src/
|       |-- main/java/com/axonivy/market/        # website/admin backend code
|       |   |-- config/                          # async, cache, scheduling, cookie, web config
|       |   |-- controller/                      # REST endpoints for products, images, OAuth, monitoring, sync
|       |   |-- github/service/impl/             # GitHub repository access and marketplace sync logic
|       |   |-- repository/                      # app-specific repositories and custom queries
|       |   |-- schedulingtask/                  # scheduled jobs and sync orchestration
|       |   |-- service/impl/                    # main business logic for product/version/content/analytics
|       |   `-- util/                            # parsing and transformation helpers like ProductContentUtils
|       |-- main/resources/
|       |   |-- application.yaml                 # app runtime config: DB, JWT, encryption, CORS
|       |   `-- db/migration/                    # Flyway migrations; schema changes go here
|       `-- test/                                # app tests for controller/service/repository behavior
`-- stable/                         # read-only API for stable/public consumers
    |-- pom.xml                     # lightweight runtime module depending on core
    |-- Dockerfile                  # container build for stable runtime
    `-- src/
        |-- main/java/com/axonivy/market/stable/ # stable API code; no write behavior expected here
        |   |-- config/                          # stable web configuration
        |   |-- controller/                      # read-only endpoints for product/details/image access
        |   |-- factory/                         # version selection factories
        |   |-- service/impl/                    # stable product and version read behavior
        |   `-- strategy/impl/                   # matching strategies such as same-major selection
        |-- main/resources/application.yaml      # stable runtime config: DB, CORS, actuator
        `-- test/                                # stable tests for controller/service/strategy behavior
```

## Setup And Build Commands

- Build all modules:
  - `mvn clean install`
- Build one module:
  - `mvn -f core/pom.xml clean install`
  - `mvn -f app/pom.xml clean install`
  - `mvn -f stable/pom.xml clean install`
- Run app:
  - `mvn -f app/pom.xml spring-boot:run`
- Run stable:
  - `mvn -f stable/pom.xml spring-boot:run`

Required environment variables:

- Shared DB:
  - `POSTGRES_HOST_URL`
  - `POSTGRES_USERNAME`
  - `POSTGRES_PASSWORD`
- App only:
  - `GITHUB_TOKEN`

Runtime defaults:

- `app`: port `8080`, context path `/app`
- `stable`: port `8085`, context path `/stable`

## Testing Instructions

- Run all tests:
  - `mvn test`
- Run module tests:
  - `mvn -f core/pom.xml test`
  - `mvn -f app/pom.xml test`
  - `mvn -f stable/pom.xml test`
- Run verification with reports:
  - `mvn -f app/pom.xml clean verify`
  - `mvn -f stable/pom.xml clean verify`
- Before changing shared logic in `core`, test both `app` and `stable`.
- Before changing SQL migrations or repository queries, prefer `clean verify` for the affected module.

## Coding Style Guidelines

- Keep changes module-local unless the behavior is truly shared.
- Put shared business logic in `core`, not duplicated in `app` and `stable`.
- Follow existing package layout and naming:
  - controllers in `controller/`
  - service implementations in `service/impl/`
  - repository customizations in `repository/impl/`
  - selection logic in `strategy/impl/` or `factory/`
- Reuse existing utilities before adding new helpers:
  - `CoreVersionUtils`
  - `ProductContentUtils`
- Prefer existing version-selection patterns over ad hoc comparisons.
- Keep Spring configuration in `application.yaml` or existing `config/` classes, not scattered in services.
- Do not introduce broad refactors when the task is a local bugfix.

## Security And Constraints

- Do not commit secrets or hardcode credentials.
- Treat these as sensitive inputs:
  - `GITHUB_TOKEN`
  - DB credentials
  - JWT and encryption settings in `app/src/main/resources/application.yaml`
- Flyway migrations must be additive and ordered. Do not edit old applied migrations unless explicitly required.
- Ignore generated or runtime folders for normal code work:
  - `*/target/`
  - `app/logs/`
  - `app/data/`
  - `app/unzip/`
- `stable` is read-only by design. Avoid introducing write behavior there.
- If a change affects API contracts, inspect controllers and OpenAPI output impact.

## Maintenance

- Keep this file concise and task-oriented.
- Update this file when module boundaries, commands, or key lookup paths change.
- If a sub-area grows more complex, add a nested `AGENTS.md` closer to that code instead of bloating this file.
