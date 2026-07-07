# AxonIvy Marketplace

Complete marketplace platform for AxonIvy products with UI, backend services, and deployment infrastructure.

## Architecture

The project consists of three main components:

### Marketplace UI
**Frontend**: Angular 18 single-page application  
**Technology**: Angular, TypeScript, SCSS  
**Purpose**: Web interface for browsing and managing products  
**Setup**: See [marketplace-ui/README.md](marketplace-ui/README.md)

### Marketplace Service  
**Backend**: Spring Boot 3.2.5 microservices with three specialized modules  
**Technology**: Java 21 LTS, PostgreSQL, Spring Data JPA, Maven  
**Modules**: 
- **Core**: Shared data models and services
- **App**: Full CRUD marketplace API (port 8080)
- **Stable**: Read-only API for integrations (port 8085)
**Setup**: See [marketplace-service/README.md](marketplace-service/README.md)

### Marketplace Build
**Infrastructure**: Docker configurations, NGINX reverse proxy, Matomo analytics  
**Purpose**: Containerization and deployment configuration  
**Setup**: See [marketplace-build/README.md](marketplace-build/README.md)

## Quick Start

### Prerequisites
- Node.js 18+ (for UI)
- JDK 21+ (for service)
- Maven 3.9+ (for service)
- PostgreSQL 17+ (for service)
- Docker & Docker Compose (for deployment)

### Setup Steps

1. **Clone and navigate to workspace**
   ```bash
   cd marketplace
   ```

2. **Start Marketplace Service** (if using database)
   ```bash
   See marketplace-service/README.md for environment setup
   mvn -f marketplace-service clean install
   mvn -f marketplace-service/app spring-boot:run  # Port 8080
   ```

3. **Start Marketplace UI**
   ```bash
   See marketplace-ui/README.md for setup
   npm install
   npm start
   ```

4. **Optional: Start with Docker**
   ```bash
   cd marketplace-build
   docker-compose up
   ```

## Project Structure

```
marketplace/
├── marketplace-ui/          # Angular frontend
├── marketplace-service/     # Spring Boot backend
│   ├── core/               # Shared models & services
│   ├── app/                # Production API
│   └── stable/             # Read-only API
├── marketplace-build/       # Docker & deployment
```

## Key Technologies

- **Frontend**: Angular 18, TypeScript, SCSS
- **Backend**: Java 21 LTS, Spring Boot 3.2.5, Spring Data JPA
- **Database**: PostgreSQL
- **Build**: Maven, npm
- **Deployment**: Docker, NGINX, Docker Compose
- **API Docs**: Swagger/OpenAPI 3.1

## Documentation

- [Marketplace Service Guide](marketplace-service/README.md) - Backend setup & API documentation
- [Marketplace UI Guide](marketplace-ui/README.md) - Frontend setup & development
- [Marketplace Build Guide](marketplace-build/README.md) - Deployment configuration

## Translation

[![translation-status](https://hosted.weblate.org/widget/axonivy-marketplace/svg-badge.svg)](https://hosted.weblate.org/engage/axonivy-marketplace/)

AxonIvy Marketplace uses Weblate for collaborative translation management. To contribute translations, visit the [AxonIvy Marketplace project on Weblate](https://hosted.weblate.org/projects/axonivy-marketplace/#information).

## Common Tasks

### Run Tests
```bash
# Backend
mvn -f marketplace-service test

# Frontend
cd marketplace-ui && npm test
```

### Generate API Documentation
Backend: http://localhost:8080/swagger-ui/index.html (App module)  
Backend: http://localhost:8085/swagger-ui/index.html (Stable module)  
Frontend: See marketplace-ui/README.md

### Build for Production
See deployment sections in individual component READMEs.

## Support & Contributing

For issues, questions, or contributions, refer to:
- [Code of Conduct](CODE_OF_CONDUCT.md)
- [License](LICENSE)
- [Security Policy](SECURITY.md)
