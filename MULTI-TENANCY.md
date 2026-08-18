# Multi-tenancy

## Resolution

```
Bearer JWT  →  Spring Security  →  claim tenant_id  →  Arconia TenantContext
```

Configuration:

```yaml
arconia:
  multitenancy:
    resolution:
      http:
        resolution-mode: oauth2
        oauth2:
          claim-name: tenant_id
```

`TenantDetailsService` loads tenants from the `tenants` table. Only `ACTIVE` tenants are `enabled()`. `SUSPENDED` and `DISABLED` fail verification.

## Isolation model (implemented)

**Shared database / shared schema** with a discriminator column.

| Table          | Tenant-aware                                                    |
| -------------- | --------------------------------------------------------------- |
| `tenants`      | no (platform)                                                   |
| `users`        | yes (`@TenantId`) — read-only Keycloak projection (`GET /api/users`) |
| `customers`    | yes (`@TenantId`)                                               |
| `orders`       | yes (`@TenantId`)                                               |
| `wallets`      | yes (`@TenantId`)                                               |
| `payments`     | yes (`@TenantId`)                                               |
| `payment_transactions` | yes (`@TenantId`)                                         |
| `ledger_entries` | yes (`@TenantId`)                                             |
| `settlements`  | yes (`@TenantId`)                                               |
| `audit_events` | column present; queried explicitly (platform admin can see all) |
| `outbox_events` | column present; no `@TenantId` (publisher has no JWT)          |
| `idempotency_keys` | column present; unique `(tenant_id, key)`                   |

Hibernate binds the discriminator from `TenantContext` through `HibernateTenantIdentifierResolver`.

## Variants (not implemented)

| Criterion      | Shared schema (A) | Schema per tenant (B) | Database per tenant (C) |
| -------------- | ----------------- | --------------------: | ----------------------: |
| Cost           | Very low          |                Medium |                    High |
| Isolation      | Medium            |                Strong |             Very strong |
| Scalability    | Strong            |                Strong |                Variable |
| Maintenance    | Simple            |                Medium |                 Complex |
| Migrations     | Simple            |                Harder |                 Complex |
| Enterprise fit | Medium            |                Strong |             Very strong |

A is the right default for this demo: one Flyway history, cheap local Docker, and still enough isolation when combined with JWT-bound context + Hibernate discriminator + tests that forge headers.

B/C become interesting for FinTech/payment workloads with noisy neighbours or contractual data-residency rules. See [ADR-001](ADR/001-multi-tenancy-strategy.md) and [ADR-004](ADR/004-shared-schema-vs-schema-per-tenant.md).
