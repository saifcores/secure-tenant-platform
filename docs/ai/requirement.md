# SecureTenant Platform

Source: user brief (cahier des charges), 2026-08-18. Updated after V2 shipped.

## V1 (MVP) — shipped

Spring Boot 4.1 resource server, Keycloak, Arconia oauth2 tenant resolution, shared-schema PostgreSQL, Flyway, Testcontainers, Prometheus/Grafana, documentation + ADRs.

## V2 (payments) — shipped

Payment domain on the same shared schema: wallet reservation, simulated PSP, double-entry ledger, settlement, idempotency, retry/timeout, outbox → Kafka, reconciliation.

Out of scope: schema-per-tenant / database-per-tenant runtimes, Keycloak Admin API for user provisioning (passwords stay in the IdP; `users` is a read-only tenant projection).
