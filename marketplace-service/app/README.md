# Marketplace App Module

Production Spring Boot 3.2.5 API for AxonIvy Marketplace with full CRUD operations, product management, and GitHub integration.

## Overview

Complete product marketplace API with full CRUD operations. Manages products, images, metadata, and Maven artifacts. Integrates with GitHub for version indexing. Depends on Core module for entities and repositories.

- **Full CRUD APIs**: GET/POST/PUT/DELETE operations
- **Product Management**: Create, update, archive products
- **GitHub Integration**: Automatic version indexing from GitHub releases, manage GitHub repositories build status,...
- **Maven Artifacts**: Index and track Maven artifact versions
- **Port**: 8080

## Technology

Java 21 LTS • Spring Boot 3.2.5 • PostgreSQL • Lombok • Spring Data JPA

## Quick Start

### Prerequisites
JDK 21+, Maven 3.9+, PostgreSQL 16+

### Environment
See [Marketplace Service](../README.md#environment-setup) for environment variable configuration. App module also requires `GITHUB_TOKEN`.

### Build & Run
marketplace-service `App` project base on `Core` project, thus you must build core project first.

```bash
mvn -f core/pom.xml clean install
mvn -f app/pom.xml clean install
mvn -f app/pom.xml spring-boot:run
```

Runs on **http://localhost:8080**

## Configuration

```yaml
spring:
  datasource:
    url: ${POSTGRES_HOST_URL}
    username: ${POSTGRES_USERNAME}
    password: ${POSTGRES_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
server:
  port: 8080
```

Set GitHub token:
```bash
echo $GITHUB_TOKEN > github.token
```



## API Endpoints

API Docs: http://localhost:8080/swagger-ui/index.html

## Testing

```bash
mvn -f app/pom.xml test
mvn -f app/pom.xml clean verify
```

## Deployment

### Docker
```bash
docker build -f app/Dockerfile -t marketplace-app .
docker run -p 8080:8080 \
  -e POSTGRES_HOST_URL=jdbc:postgresql://localhost:5432/marketplace \
  -e POSTGRES_USERNAME=postgres \
  -e POSTGRES_PASSWORD=password \
  -e GITHUB_TOKEN=token \
  marketplace-app
```

## Database Migrations (Flyway)

### Script Location

Source (edit here):
```
app/src/main/resources/db/migration/
```

After build, scripts are copied to the classpath and Flyway reads them from:
```
app/target/classes/db/migration/
```

This matches the Flyway configuration:
```yaml
flyway:
  locations: classpath:db/migration
  baseline-on-migrate: true
  baseline-version: 1
```

### Naming Convention

```
V{YYYYMMDDHHmm}__{description}.sql
```

| Part | Rule | Example |
|---|---|---|
| `V` | Must start with uppercase V | `V` |
| `YYYYMMDDHHmm` | Timestamp, ensures chronological order | `202606031045` |
| `__` | Double underscore separator (required) | `__` |
| `description` | Lowercase words separated by `_` | `add_user_table` |
| `.sql` | File extension | `.sql` |

**Examples:**
```
V202606031045__create_external_document_meta.sql
V202606031200__add_sync_task_type_check.sql
V202606031400__drop_columns_product.sql
```

> ⚠️ A single underscore (`V202606031045_description.sql`) will be silently ignored by Flyway.

> ℹ️ `V1__init_schema.sql` is the baseline. All new scripts must have a version greater than `1` (any timestamp qualifies).

## Related

- [Core Module](../core/README.md)
- [Stable Module](../stable/README.md)
- [Marketplace Service](../README.md)
