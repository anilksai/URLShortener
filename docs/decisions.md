# Decision Log

Last updated: 2026-08-15

Purpose
- Capture high-level architecture, security, observability and orchestration decisions for this prototype.

Decisions
- Short-code generation: Use URL-safe Base64 with SecureRandom (Base64ShortCodeService). Rationale: cryptographically-strong randomness, URL-safe characters, avoids bias from simple alphabets.
- Short-code DI: Introduced ShortCodeService interface and injected implementation for testability and swap-in flexibility.
- URL safety: UrlSafetyGuard normalizes URLs, rejects non-http(s), userinfo, fragments, control characters, and private/local IP targets to mitigate SSRF.
- Orchestration: Added OrchestratorEngine (queue-backed) with retries, exponential backoff, and Micrometer metrics. Rationale: decouples submission from execution and improves observability.
- Metrics: Integrated Micrometer and Prometheus registry; per-node tagged counters for fine-grained observability.

Risks & Mitigations
- SS: Current UrlSafetyGuard performs DNS lookups which may be slow — consider caching and asynchronous validation.
- Durability: OrchestratorEngine is in-memory and single-node; for production use a durable queue (Kafka/Rabbit/SQS) and multiple workers.
- Collision resistance: Short-code length and randomness should be tuned for production and consider persistent uniqueness checks and alternate strategies.

References
- README.md (project overview)
- src/main/java/com/example/demo/util/Base64ShortCodeService.java
- src/main/java/com/example/demo/util/UrlSafetyGuard.java
- src/main/java/com/example/demo/orchestration/OrchestratorEngine.java

Change log
- 2026-08-15: Added Base64 short codes, DI, hardened URL validation, OrchestratorEngine, Micrometer integration, and per-node metrics.
