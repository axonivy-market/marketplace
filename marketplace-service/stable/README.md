# Marketplace Stable Module

A stable Spring Boot 3.2.5 application that provides a public API for accessing product information and images from the AxonIvy Marketplace. This module serves as a read-only, stable interface for external consumers.

## Overview

The **Stable** module is a Spring Boot application that exposes REST APIs for:
- **Products**: Retrieve product information, versions, and detailed metadata
- **Images**: Access product images and related visual assets

This module depends on the **core** module, which contains shared data models, repositories, and business logic.

## Technology Stack

- **Java**: 21 LTS
- **Spring Boot**: 3.2.5
- **Database**: PostgreSQL
- **Build Tool**: Maven 3.x
- **Code Generation**: Project Lombok
- **Documentation**: Swagger/OpenAPI 3.1
- **Packaging**: WAR format
- **Server Port**: 8085

## Prerequisites

- Java Development Kit (JDK) 21 or higher
- Apache Maven 3.6+
- PostgreSQL 12+ (running and accessible)
- Lombok IDE support (optional, for development)

## Setup & Configuration

### 1. Database Configuration

The application requires PostgreSQL. Configure the connection via environment variables or modify `src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: ${POSTGRES_HOST_URL}          # e.g., jdbc:postgresql://localhost:5432/marketplace
    username: ${POSTGRES_USERNAME}     # PostgreSQL username
    password: ${POSTGRES_PASSWORD}     # PostgreSQL password
    driver-class-name: org.postgresql.Driver
```

### 2. Environment Variables

Set the following before running:

```bash
export POSTGRES_HOST_URL=jdbc:postgresql://localhost:5432/marketplace
export POSTGRES_USERNAME=postgres
export POSTGRES_PASSWORD=your_password
export BUILD_VERSION=1.0.0
```

## Building the Project

```bash
# Clean build
mvn clean install

# Build without running tests
mvn clean install -DskipTests

# Build with specific JDK path
mvn clean install -Djava.home=/path/to/jdk21
```

## Running the Application

### Option 1: Using Maven
```bash
mvn spring-boot:run
```

### Option 2: Running the WAR directly
```bash
java -jar target/stable-1.0.0-SNAPSHOT.war
```

### Option 3: Docker (if Dockerfile is available)
```bash
docker build -t marketplace-stable .
docker run -p 8085:8085 -e POSTGRES_HOST_URL=jdbc:postgresql://host.docker.internal:5432/marketplace marketplace-stable
```

The application will start on **http://localhost:8085**

## API Documentation

Once running, access the interactive API documentation at:

- **Swagger UI**: http://localhost:8085/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8085/v3/api-docs

### Main Endpoints

- **GET /product** - List all products with pagination
- **GET /product/{id}/versions** - Get available versions for a product
- **GET /product/{id}/version/{version}/content** - Get product details and metadata
- **GET /image** - Access image resources

## Testing

### Run all tests
```bash
mvn test
```

### Run a specific test class
```bash
mvn test -Dtest=ProductControllerTest
```

### Run a specific test method
```bash
mvn test -Dtest=ProductControllerTest#testFindProducts
```

### Generate code coverage report
```bash
mvn clean verify
# Report location: target/site/jacoco/index.html
```

## Development

### IDE Setup - Eclipse

1. **Install Lombok**:
   - Download from https://projectlombok.org/download
   - Run: `java -jar lombok.jar`
   - Point to your Eclipse installation
   - Restart Eclipse

2. **Import Project**:
   - File → Import → Existing Maven Projects
   - Select the `stable` directory
   - Right-click project → Maven → Update Project

3. **Running from IDE**:
   - Right-click `MarketplaceStableApplication.java` → Run As → Java Application
   - Ensure environment variables are set in Run Configurations

### IDE Setup - IntelliJ IDEA

1. **Enable Annotation Processing**:
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check "Enable annotation processing"

2. **Run Configuration**:
   - Edit Configurations → New Run Configuration (Application)
   - Main class: `com.axonivy.market.stable.MarketplaceStableApplication`
   - Set environment variables for PostgreSQL

## Code Quality & Analysis

### SonarQube Analysis
```bash
mvn clean verify sonar:sonar -Dsonar.host.url=http://localhost:9000
```

### JaCoCo Code Coverage
The jacoco-maven-plugin is configured to generate coverage reports:
```bash
mvn clean verify
# Open: target/site/jacoco/index.html
```

## Project Structure

```
stable/
├── pom.xml                                    # Maven configuration
├── Dockerfile                                 # Docker image definition
├── README.md                                  # This file
├── src/
│   ├── main/
│   │   ├── java/com/axonivy/market/stable/
│   │   │   ├── MarketplaceStableApplication.java    # Spring Boot entry point
│   │   │   ├── controller/                           # REST controllers
│   │   │   │   ├── ProductController.java            # Product endpoints
│   │   │   │   └── ImageController.java              # Image endpoints
│   │   │   └── service/                              # Business logic
│   │   └── resources/
│   │       └── application.yaml                      # Application properties
│   └── test/
│       └── java/com/axonivy/market/stable/           # Unit tests
└── target/                                   # Build output (generated)
```

## Troubleshooting

### Issue: PostgreSQL connection failed
**Solution**: Verify PostgreSQL is running and environment variables are correctly set:
```bash
echo $POSTGRES_HOST_URL
echo $POSTGRES_USERNAME
```

### Issue: Port 8085 already in use
**Solution**: Change the port in `application.yaml`:
```yaml
server:
  port: 8086
```

### Issue: Build fails with "Lombok annotation not recognized"
**Solution**: 
- Ensure Lombok is properly installed in your IDE
- Run: `mvn clean install`
- Restart your IDE

### Issue: Tests fail after code changes
**Solution**: Rebuild with clean:
```bash
mvn clean test
```

## Performance Configuration

The application is configured with:
- **Max Threads**: 200
- **Connection Timeout**: 240 seconds
- **Max Swallow Size**: Unlimited (-1)

Adjust these in `application.yaml` if needed for your environment.

## References

- [Spring Boot 3.2.5 Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA Guide](https://spring.io/projects/spring-data-jpa)
- [Spring Web Reference](https://docs.spring.io/spring-boot/docs/3.2.5/reference/htmlsingle/index.html#web)
- [Apache Maven Documentation](https://maven.apache.org/guides/index.html)
- [Project Lombok](https://projectlombok.org/)
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/)
