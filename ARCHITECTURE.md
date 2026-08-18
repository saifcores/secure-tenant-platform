# Architecture

SecureTenant is a modular monolith. Packages are split by **bounded context**, then by **hexagonal layers** (`domain`, `application`, `infrastructure`, `api`). There is no global `controller` / `service` / `repository` dump.

```
com.example.securetenant
├── tenant      organisations on the platform
├── identity    JWT principal, roles (passwords stay in Keycloak)
├── customer    tenant-scoped CRM
├── order       tenant-scoped order lifecycle
├── payment     orchestration, PSP, transactions, settlements
├── wallet      available / reserved balances
├── ledger      double-entry postings
├── outbox      transactional events → Kafka
├── idempotency payment creation keys
├── audit       sensitive-operation trail
├── security    Spring Security, JWT, Hibernate tenant resolver
└── shared      errors, metrics, persistence base types
```

## Runtime flow

```
HTTP + Bearer JWT
        │
        ▼
Spring Security (issuer, audience, exp, signature, roles)
        │
        ▼
Arconia TenantContextFilter (oauth2 claim tenant_id)
        │
        ▼
TenantDetailsService (tenant exists and is ACTIVE)
        │
        ▼
Application service  ──►  Hibernate @TenantId discriminator
        │
        ▼
PostgreSQL (shared database, shared schema)
```

The effective tenant is **never** taken from `X-Tenant-ID`, query parameters, or the body. Arconia is configured with `resolution-mode: oauth2` and claim `tenant_id`. A forged header is ignored.

## Isolation layers (defence in depth)

1. **Authentication** — only Keycloak-issued JWTs are accepted.
2. **RBAC** — HTTP matcher rules + `@PreAuthorize`.
3. **Tenant context** — Arconia binds `TenantContext` from the JWT.
4. **Tenant verification** — unknown / suspended / disabled tenants are rejected.
5. **Persistence** — Hibernate `@TenantId` on tenant-owned money and CRM tables so every query and insert is discriminator-filtered.
6. **API mapping** — missing cross-tenant rows surface as `404`, not `200` with another tenant's payload.

## Persistence strategy (MVP)

Shared database / shared schema. `tenant_id` is a column on tenant-owned tables. See [MULTI-TENANCY.md](MULTI-TENANCY.md) and [ADR-001](ADR/001-multi-tenancy-strategy.md).

Platform-level tables (`tenants`) have no discriminator so `ROLE_PLATFORM_ADMIN` can manage organisations without a `tenant_id` claim. Those routes are ignored by the Arconia filter (`/api/tenants/**`).

## Observability

- Structured JSON logs (Spring Boot Logstash format) with `tenantId` (Arconia MDC) and `userId`.
- Micrometer metrics: `http.server.requests`, `orders_created_total`, `orders_failed_total`, `payments_created_total`, `payments_settled_total`, `payments_failed_total`, `payments_retried_total`, `security_denied_total`, `tenant_requests_total`.
- OpenTelemetry traces via Arconia (`HTTP → security → tenant → service → JDBC`).
- Prometheus scrapes `/actuator/prometheus`; Grafana loads a provisioned dashboard.
