# Marketplace Stable Module

Read-only Spring Boot 3.2.5 API for Neo Designer, VSCode Extension v14, and AI-powered features.

## Overview

Stable, versioned product information as read-only APIs. Depends on Core module for entities and repositories.

- **Read-only APIs**: GET operations only
- **Versioned Endpoints**: Version-specific metadata
- **AI-Ready**: Extended metadata for processing
- **Port**: 8085

## Technology

Java 21 LTS • Spring Boot 3.2.5 • PostgreSQL • Lombok

## Quick Start

### Prerequisites
JDK 21+, Maven 3.6+, PostgreSQL 12+

### Environment
```bash
export POSTGRES_HOST_URL=jdbc:postgresql://localhost:5432/marketplace
export POSTGRES_USERNAME=postgres
export POSTGRES_PASSWORD=your_password
```

### Build & Run
```bash
mvn -f stable/pom.xml clean install
mvn -f stable/pom.xml spring-boot:run
```

Runs on **http://localhost:8085**

## API Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /product` | List all products |
| `GET /product/{id}` | Get product details |
| `GET /product/{id}/versions` | Get available versions |
| `GET /product/{id}/version/{version}/content` | Get version metadata |
| `GET /image` | List product images |

API Docs: http://localhost:8085/swagger-ui/index.html

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
  port: 8085
```

## Testing

```bash
mvn -f stable/pom.xml test
mvn -f stable/pom.xml clean verify
```

## Deployment

### Docker
```bash
docker build -f stable/Dockerfile -t marketplace-stable .
docker run -p 8085:8085 \
  -e POSTGRES_HOST_URL=jdbc:postgresql://localhost:5432/marketplace \
  -e POSTGRES_USERNAME=postgres \
  -e POSTGRES_PASSWORD=password \
  marketplace-stable
```

### WAR
```bash
mvn -f stable/pom.xml clean package
cp stable/target/stable-1.0.0-SNAPSHOT.war $CATALINA_HOME/webapps/
```

## Troubleshooting

**Port 8085 in use**: Change `server.port` in application.yaml

**Database connection failed**: Check environment variables and PostgreSQL running

**Tests failing**: Verify database credentials

## Related

- [Core Module](../core/README.md)
- [App Module](../app/README.md)
- [Marketplace Service](../README.md)
