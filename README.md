URLShortener - Prototype

This repository is a prototype URL shortener implemented with Spring Boot, H2 (in-memory), layered architecture (Controller -> Service -> Repository), DTOs, validation, and a small orchestration prototype.

Features
- Health check: GET /health or /api/health
- Create short URLs: POST /api/shorten { "originalUrl": "https://..." }
- Redirect: GET /{code} (302 -> original URL)
- Analytics: GET /api/analytics/{code}
- Global exception handling and validation
- In-memory H2 database for prototype
- Prototype orchestration component (com.example.demo.orchestration.Orchestrator)

Build & Run
1. If you have Maven installed:
   mvn -DskipTests package
   mvn spring-boot:run

2. If Maven is not installed, add the Maven Wrapper locally (recommended):
   mvn -N io.takari:maven:wrapper
   ./mvnw -DskipTests package
   ./mvnw spring-boot:run

Running tests
- With Maven: mvn test
- With wrapper: ./mvnw test

APIs
- POST /api/shorten  -> returns { code, shortUrl }
- GET /{code}        -> redirects (302) to original URL
- GET /api/analytics/{code} -> returns analytics data

API docs (OpenAPI / Swagger)
- Interactive Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

The project includes an OpenAPI config in com.example.demo.config.OpenApiConfig to provide API metadata.

Notes
- This is a prototype focusing on clean layering, SOLID principles, DTOs, and exception handling.
- For production: add persistent DB (Postgres), rate limits, authentication, observability exporters, and more robust code generation for collision resistance.

Next steps (available on request)
- Add GitHub Actions CI for build/tests
- Add Postgres and Flyway migrations
- Add quotas, authentication, and rate limiting
- Harden the orchestration layer to persist state and expose a UI
