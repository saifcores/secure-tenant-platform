# ADR-004 — Shared schema vs schema per tenant

## Status

Accepted (shared schema for MVP)

## Context

The brief asks for a comparison of:

- A — shared DB, shared schema
- B — shared DB, schema per tenant
- C — database per tenant

## Decision

Ship **A**. Document B and C as evolution paths, not as concurrently supported runtimes.

Use Hibernate discriminator (`@TenantId`) so isolation is not “remember to add `where tenant_id = ?` on every query”.

## Consequences

- Fastest path to a convincing security demo and CI with one Postgres container.
- Connection routing, Flyway per schema/database, and provisioning automation are deferred.
- Payment/FinTech V2 shipped on strategy A without changing the decision. B or C remain evolution paths for contractual isolation.
