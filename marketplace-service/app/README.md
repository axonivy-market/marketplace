# Marketplace App Module

Production Spring Boot 3.2.5 API for AxonIvy Marketplace with full CRUD operations, product management, and GitHub integration.

## Overview

Complete product marketplace API with full CRUD operations. Manages products, images, metadata, and Maven artifacts. Integrates with GitHub for version indexing. Depends on Core module for entities and repositories.

- **Full CRUD APIs**: GET/POST/PUT/DELETE operations
- **Product Management**: Create, update, delete products
- **Image Handling**: Upload and manage product images
- **GitHub Integration**: Automatic version indexing from GitHub releases
- **Maven Artifacts**: Index and track Maven artifact versions
- **Port**: 8080

## Technology

Java 21 LTS • Spring Boot 3.2.5 • PostgreSQL • Lombok • Spring Data JPA

## Quick Start

### Prerequisites
JDK 21+, Maven 3.6+, PostgreSQL 12+

### Environment
See [Marketplace Service](../README.md#environment-setup) for environment variable configuration. App module also requires `GITHUB_TOKEN`.

### Build & Run
```bash
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
      ddl-auto: update
server:
  port: 8080
```

Set GitHub token:
```bash
echo $GITHUB_TOKEN > github.token
```

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/product` | GET/POST | List all or create product |
| `/product/{id}` | GET/PUT/DELETE | Product details, update, delete |
| `/product/{id}/versions` | GET | Available versions |
| `/image` | GET/POST | List or upload images |

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

### WAR
```bash
mvn -f app/pom.xml clean package
cp app/target/app-1.0.0-SNAPSHOT.war $CATALINA_HOME/webapps/
```

## Troubleshooting

**Port 8080 in use**: Change `server.port` in application.yaml

**Database connection failed**: Check environment variables and PostgreSQL running

**GitHub integration fails**: Verify GITHUB_TOKEN is set correctly

## Related

- [Core Module](../core/README.md)
- [Stable Module](../stable/README.md)
- [Marketplace Service](../README.md)
