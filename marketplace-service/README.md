# Marketplace Service

Spring Boot 3.2.5 backend application for AxonIvy Marketplace with three specialized Maven modules.

## Modules

### Core Module
**Shared foundation**: Data entities, repositories, services, utilities, and constants  
**Key classes**: Product, Metadata, Image entities; CoreProductService, CoreVersionService  
**No dependencies**: Provides base for App and Stable modules  
**Build**: `mvn -f core/pom.xml clean install`  
**Details**: See [core/README.md](core/README.md)

### App Module
**Production marketplace API**: Full CRUD operations for products, images, metadata  
**Features**: Product management, image handling, GitHub integration, Maven artifact indexing  
**Server Port**: 8080  
**Endpoints**: GET/POST/PUT/DELETE /product, /image  
**Special**: Requires GitHub token in `github.token` file  
**Build**: `mvn -f app/pom.xml clean install`  
**Run**: `mvn -f app/pom.xml spring-boot:run`  
**API Docs**: http://localhost:8080/swagger-ui/index.html  
**Details**: See [app/README.md](app/README.md)

### Stable Module
**Read-only API**: For new integrations (Neo Designer, VSCode v14, AI features)  
**Features**: Versioned product queries, image access, optimized for reads  
**Server Port**: 8085  
**Endpoints**: GET /product, /product/{id}/versions, /product/{id}/version/{version}/content, /image  
**Build**: `mvn -f stable/pom.xml clean install`  
**Run**: `mvn -f stable/pom.xml spring-boot:run`  
**API Docs**: http://localhost:8085/swagger-ui/index.html  
**Details**: See [stable/README.md](stable/README.md)

## Quick Start

### Prerequisites
- JDK 21+
- Maven 3.6+
- PostgreSQL 12+

### Environment Setup
```bash
export POSTGRES_HOST_URL=jdbc:postgresql://localhost:5432/marketplace
export POSTGRES_USERNAME=postgres
export POSTGRES_PASSWORD=your_password
export BUILD_VERSION=1.0.0
export GITHUB_TOKEN=your_token  # App module only
```

### Build All
```bash
mvn clean install
```

### Run Individual Modules
```bash
# App Module (Port 8080)
mvn -f app/pom.xml spring-boot:run

# Stable Module (Port 8085)
mvn -f stable/pom.xml spring-boot:run
```

## Module Dependencies

```
app ──┐
      ├─→ core
      │
stable┘
```

Both App and Stable depend on Core.

## Technology

- **Java**: 21 LTS
- **Spring Boot**: 3.2.5
- **Database**: PostgreSQL with Spring Data JPA
- **Build Tool**: Maven
- **Code Gen**: Project Lombok
- **API Documentation**: Swagger/OpenAPI 3.1

## Testing

```bash
# All tests
mvn test

# Specific module
mvn -f app/pom.xml test

# Code coverage
mvn clean verify
```

## References

- [Core Module](core/README.md)
- [App Module](app/README.md)
- [Stable Module](stable/README.md)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
