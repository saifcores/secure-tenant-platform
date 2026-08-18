# Security

## Authentication

Keycloak is the only identity provider. The API is a pure OAuth2/OIDC **resource server**. It never stores or checks passwords.

JWT validation (Spring Security + Nimbus):

- **Signature** against the realm JWKS
- **Issuer** (`iss`) equals `…/realms/securetenant`
- **Audience** (`aud`) contains `securetenant-api`
- **Expiration** (`exp`) / not-before

Expired or malformed tokens return `401 Unauthorized` with the standard error envelope (no stack traces).

## Authorization

Two questions are always answered:

1. Does the **role** allow the action?
2. Does the **resource** belong to the authenticated tenant?

Roles (realm roles, mapped to `ROLE_*`):

| Role             | Typical permissions |
| ---------------- | ------------------- |
| `PLATFORM_ADMIN` | Tenants, global audit |
| `TENANT_ADMIN`   | Tenant user directory (read), customers, orders, payments, wallets, settlements, reconciliation, tenant audit |
| `MANAGER`        | Customers write, order confirm/cancel/complete, payment create/retry/cancel, stats, wallets |
| `USER`           | Read customers/orders/payments/wallets; create orders and payments |
| `AUDITOR`        | Read-only customers, orders, payments, wallets, settlements, reconciliation, users, audit |

Cross-tenant reads resolve to **404** (the Hibernate discriminator hides the row). Insufficient role resolves to **403**.

## Tenant claim

`tenant_id` is a **user attribute** mapped into the access token by Keycloak. The API trusts that claim only after JWT validation. Headers such as `X-Tenant-ID` are not used for resolution (`arconia.multitenancy.resolution.http.resolution-mode=oauth2`).

## API hardening

- CSRF disabled (stateless bearer API)
- CORS allow-list (`app.security.cors.allowed-origins`)
- Bean Validation on write DTOs
- `server.error.include-stacktrace=never`
- Secrets via environment variables, not Git
- HTTPS is expected in front of the API in production (compose uses HTTP for local demo)

## Mandatory scenarios

| Scenario | Expected |
| -------- | -------- |
| Alice (acme) `GET /api/orders` | 200, acme rows only |
| Alice reads a globex order id | 404 |
| Alice sends `X-Tenant-ID: globex` | still acme |
| Bob (`USER`) `DELETE /api/customers/{id}` | 403 |
| Bob (`USER`) `GET /api/users` | 403 |
| Expired / invalid JWT | 401 |
| Unknown or suspended tenant | 4xx from tenant verification |
| Alice pays a globex order | 404 |
| Alice pays an unconfirmed order | 422 |
| Alice forges `X-Tenant-ID` on `GET /api/wallets` | still acme wallets |
