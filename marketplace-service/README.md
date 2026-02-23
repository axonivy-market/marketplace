# Marketplace Service

Spring Boot 3.2.5 backend application for AxonIvy Marketplace with three specialized Maven modules.

## Modules

### Core Module
**Shared foundation**: Data entities, repositories, services, utilities  
**Key components**: Product, Metadata, Image entities; service layer for all modules  
**Dependency**: Base module used by App and Stable  
**Learn more**: See [core/README.md](core/README.md)

### App Module
**Production marketplace API**: Full CRUD operations with GitHub integration  
**Features**: Product management, image handling, Maven artifact indexing  
**API Port**: 8080  
**Learn more**: See [app/README.md](app/README.md)

### Stable Module
**Read-only API**: For Neo Designer, VSCode v14, and AI features  
**Features**: Versioned product queries, optimized for reads  
**API Port**: 8085  
**Learn more**: See [stable/README.md](stable/README.md)

## Prerequisites

- JDK 21+
- Maven 3.6+
- PostgreSQL 12+

## Environment Setup

All modules use the same database configuration via environment variables:

```bash
export POSTGRES_HOST_URL=jdbc:postgresql://localhost:5432/marketplace
export POSTGRES_USERNAME=postgres
export POSTGRES_PASSWORD=your_password

# App module only
export GITHUB_TOKEN=your_github_token
```

## Build All Modules

```bash
mvn clean install
```

## Run Individual Modules

```bash
# Build and run App module (Port 8080)
mvn -f app/pom.xml spring-boot:run

# Build and run Stable module (Port 8085)
mvn -f stable/pom.xml spring-boot:run
```

## Module Dependencies

```
app ──┐
      ├─→ core
      │
stable┘
```

Both App and Stable depend on Core for shared data models and repositories.

## Technology Stack

Java 21 LTS • Spring Boot 3.2.5 • Spring Data JPA • PostgreSQL • Lombok • Swagger/OpenAPI 3.1

## Testing

```bash
# All modules
mvn test

# Specific module
mvn -f app/pom.xml test
mvn -f stable/pom.xml test
mvn -f core/pom.xml test
```

## API Documentation

After running either module, access API docs:

- App: http://localhost:8080/swagger-ui/index.html
- Stable: http://localhost:8085/swagger-ui/index.html

## References

- [Core Module](core/README.md) - Shared data models and services
- [App Module](app/README.md) - Production marketplace API
- [Stable Module](stable/README.md) - Read-only integrations API

