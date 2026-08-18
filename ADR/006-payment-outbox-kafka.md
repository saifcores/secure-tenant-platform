# ADR-006 — Payment outbox, idempotency, and Kafka

## Status

Accepted

## Context

V2 adds a payment domain (wallet, PSP, ledger, settlement). Events must not be lost if Kafka is down, duplicate `POST /api/payments` must not double-charge, and scheduled retry/reconciliation must run without a request-scoped JWT tenant.

## Decision

- Persist `outbox_events` in the same transaction as the payment change; a scheduled publisher pushes to Kafka topic `payments.events`.
- Require `Idempotency-Key` on payment creation; unique `(tenant_id, key)`; replay vs `409` when the stored hash (SHA-256 of `orderId`) does not match.
- Keep Hibernate `@TenantId` on money tables; **omit** it on `outbox_events` and `idempotency_keys`. Retry scans due payments with JDBC, then binds `TenantContext` per tenant.
- Disable Kafka, outbox publishing, retry, and reconciliation in the `test` profile so Testcontainers tests do not need a broker.

## Consequences

- Exactly-once *intent* at the API (idempotency) and at-least-once delivery to Kafka (outbox).
- Background jobs cannot accidentally query another tenant's wallets: they set `TenantContext` before calling the orchestrator.
- Local `spring-boot:run` works without Kafka; Compose `docker` profile enables the broker and publisher.
