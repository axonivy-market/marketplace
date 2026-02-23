# Core Module

Shared foundation library for Marketplace Service containing data models, repositories, services, and utilities used by App and Stable modules.

## What It Provides

| Component | Details |
|-----------|---------|
| **Entities** | Product, Metadata, Image, Artifact, MavenArtifactVersion, ProductJsonContent, ProductCustomSort |
| **Repositories** | Spring Data repositories with custom queries and pagination |
| **Services** | CoreProductService, CoreVersionService, CoreImageService |
| **Utils** | CoreVersionUtils (semantic versioning), CoreMavenUtils (Maven artifacts) |
| **Constants** | BasePackageConstants, database tables, API routes, request parameters |
| **Models** | ProductModel, MavenArtifactVersionModel for REST APIs |

## Technology

Java 21 LTS • Spring Boot 3.2.5 • Spring Data JPA • PostgreSQL • Lombok • Jackson

## Build

```bash
mvn -f core/pom.xml clean install
mvn -f core/pom.xml test
```

## Use as Dependency

```xml
<dependency>
  <groupId>com.axonivy.market</groupId>
  <artifactId>core</artifactId>
  <version>${project.version}</version>
</dependency>
```

Configure in your application:

```java
@SpringBootApplication(scanBasePackages = {
    CORE_BASE_PACKAGE_NAME,
    YOUR_MODULE_PACKAGE_NAME
})
@EnableJpaRepositories(basePackages = CORE_BASE_PACKAGE_REPO_NAME)
@EntityScan(basePackages = CORE_BASE_PACKAGE_ENTITY_NAME)
public class Application { }
```

## Environment Setup

```bash
export POSTGRES_HOST_URL=jdbc:postgresql://localhost:5432/marketplace
export POSTGRES_USERNAME=postgres
export POSTGRES_PASSWORD=your_password
```

## Shared Across Modules

✅ JPA entities for products, metadata, images, versions  
✅ Spring Data repositories for database operations  
✅ Business logic services (product, version, image management)  
✅ Version utilities (semantic versioning, release detection, latest version)  
✅ Constants (database tables, API routes, request parameters)  
✅ REST models and custom exceptions  

## Related

- [App Module](../app/README.md) - Production marketplace API (full CRUD)
- [Stable Module](../stable/README.md) - Read-only API (Neo Designer, VSCode v14)
