# ADR-001 — Choice of multi-tenancy strategy

## Status

Accepted

## Context

SecureTenant must host ACME, GLOBEX and FINCORP on one platform with strict isolation, while remaining easy to run locally (Docker Compose, Flyway, Testcontainers).

## Decision

Implement **shared database / shared schema** with a `tenant_id` discriminator, JWT-bound tenant context (Arconia oauth2 mode), and Hibernate `@TenantId`.

## Consequences

- One schema, one Flyway history, cheap ops.
- Isolation depends on application + ORM discipline; tests must prove header forgery cannot switch tenant.
- Schema-per-tenant and database-per-tenant remain documented options if contractual isolation is required later.
- Payment/FinTech V2 shipped on this same shared-schema strategy (see [ADR-006](006-payment-outbox-kafka.md)).
