# Architecture Overview

Overview
- URLShortener is a Spring Boot prototype implementing a small URL shortening service with safety guardrails and a lightweight orchestration prototype.

Key components
- Controller layer (UrlController, OrchestrationController): HTTP endpoints and request/response mapping.
- Service layer (UrlServiceImpl): Business logic, URL validation, short-code generation, persistence orchestration.
- Repository (ShortUrlRepository): JPA repository using H2 for the prototype; swap to Postgres for production.
- Utilities: ShortCodeService (interface) with Base64ShortCodeService; UrlSafetyGuard enforces validation and normalization.
- Orchestration: OrchestratorEngine provides queue-backed task execution with retries and Micrometer metrics.
- Observability: Micrometer + Prometheus registry exposed via Actuator (/actuator/prometheus).

Extension points and recommendations
- Replace in-memory Orchestrator queue with durable message queue and horizontally scalable workers for production.
- Externalize configuration: rate limits, allowed/blocked hosts, and short-code length.
- Add authentication/authorization to protect shortening and orchestration endpoints.
- Add monitoring dashboards (Grafana) and alerting based on orchestrator failure/saturation metrics.

Non-functional goals
- Testable: DI and clear ports/adapters enable unit testing.
- Secure-by-default: validation, SSRF mitigations, and safe short-code generation.
- Observability: metrics and actuator endpoints for simple SLI/alerts.

Contact
- Repository: anilksai/URLShortener
