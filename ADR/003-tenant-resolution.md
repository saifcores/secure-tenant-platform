# ADR-003 — Tenant resolution strategy

## Status

Accepted

## Context

A naive SaaS API accepts `tenantId` as a query parameter or `X-Tenant-ID` header. That is trivial to forge and breaks isolation.

## Decision

Resolve the tenant **only** from the validated JWT claim `tenant_id` via Arconia's HTTP `oauth2` resolution mode. Place `TenantContextFilter` after authentication (`AnonymousAuthenticationFilter`) so the principal exists before the claim is read.

Ignore `/api/tenants/**` and `/api/platform/**` so platform administrators (who may have no `tenant_id`) can still call platform APIs. `/api/platform/**` is reserved; no controllers exist yet.

## Consequences

- Forged `X-Tenant-ID` / `X-TenantId` / `?tenantId=` have no effect.
- Requests without a tenant claim on tenant-scoped APIs fail tenant resolution/verification.
