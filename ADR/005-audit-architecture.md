# ADR-005 — Audit architecture

## Status

Accepted

## Context

Sensitive operations must be attributable: who, which tenant, which resource, when, from where.

## Decision

- Synchronous write to `audit_events` in the same transaction as the business change (`AuditRecorder`).
- Fields: tenant, user (`sub`), action, resource type/id, timestamp, IP, metadata JSON.
- No Hibernate `@TenantId` on audit rows so `PLATFORM_ADMIN` can list the global trail; tenant users are filtered in `AuditQueryService`.
- Actions include `TENANT_CREATED`, `TENANT_STATUS_CHANGED`, `CUSTOMER_CREATED`, `CUSTOMER_UPDATED`, `CUSTOMER_DELETED`, `ORDER_CREATED`, `ORDER_CONFIRMED`, `ORDER_CANCELLED`, `ORDER_COMPLETED`, `PAYMENT_CREATED`, `PAYMENT_RETRY_SCHEDULED`, `PAYMENT_SETTLED`, `PAYMENT_FAILED`, `PAYMENT_CANCELLED`.

## Consequences

- Audit cannot silently disappear if the business commit succeeds.
- High write volume would later push this to an outbox + async consumer. V2 outbox is for **payment events → Kafka only**; audit remains a synchronous table write.
