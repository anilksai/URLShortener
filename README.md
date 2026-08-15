# URLShortener - Prototype

[![CI](https://github.com/anilksai/URLShortener/actions/workflows/ci.yml/badge.svg)](https://github.com/anilksai/URLShortener/actions/workflows/ci.yml) [![Build Status](https://img.shields.io/github/actions/workflow/status/anilksai/URLShortener/ci.yml?branch=main)](https://github.com/anilksai/URLShortener/actions) [![Coverage](https://img.shields.io/badge/coverage-unknown-lightgrey.svg)](https://codecov.io/gh/anilksai/URLShortener)

A small, secure URL shortening prototype built with Spring Boot. It demonstrates a clean layered architecture (Controller -> Service -> Repository), DTOs, validation, and basic analytics. The service accepts long URLs and returns short codes that redirect to the original URL while recording redirect counts.

Key features
- Health check: GET /health or /api/health
- Create short URLs: POST /api/shorten { "originalUrl": "https://..." }
- Redirect: GET /{code} (HTTP 302 -> original URL)
- Analytics: GET /api/analytics/{code} (redirect counts and metadata)
- Safety guardrails: only http/https, host validation, block local/private addresses, enforce length limits
- Secure short-code generation: URL-safe Base64 using SecureRandom
- In-memory H2 DB for prototype and a small orchestration component

Build & Run
1. If you have Maven installed:
   mvn -DskipTests package
   mvn spring-boot:run

2. Using the Maven Wrapper (recommended):
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

Notes
- Prototype goals: demonstrate secure generation, clean layering, validation, and simple analytics. Not production-ready: add persistent DB (Postgres), rate limiting, authentication, monitoring, and hardened short-code collision handling before production use.

Documentation & decision log
- docs/architecture.md: Architecture overview, components, extension points and recommendations
- docs/decisions.md: Decision log capturing rationale, risks and change history
