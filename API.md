# API

Base URL: `http://localhost:8080`

All `/api/**` routes except actuator health/info/prometheus require `Authorization: Bearer <access_token>`.

Interactive docs: **Swagger UI** at `/swagger-ui.html` (OpenAPI JSON at `/v3/api-docs`). Use **Authorize** with a Bearer token, or Keycloak password grant (`securetenant-public`, username `alice`, password `password`).

Error envelope:

```json
{
  "timestamp": "2026-08-18T09:30:00Z",
  "status": 403,
  "error": "FORBIDDEN",
  "message": "Access denied",
  "path": "/api/orders/123",
  "traceId": "7f8a..."
}
```

| Status | `error`                | When                                                                    |
| ------ | ---------------------- | ----------------------------------------------------------------------- |
| 400    | `BAD_REQUEST`          | Validation, missing `Idempotency-Key`                                   |
| 401    | `UNAUTHORIZED`         | Missing / expired / invalid JWT                                         |
| 403    | `FORBIDDEN`            | Authenticated but role insufficient                                     |
| 404    | `NOT_FOUND`            | Missing id, or another tenant's row                                     |
| 409    | `CONFLICT`             | Duplicate email, or idempotency key reused with a different `orderId`   |
| 422    | `UNPROCESSABLE_ENTITY` | Illegal state transition, unpaid/unconfirmed order, insufficient wallet |

`/api/platform/**` is reserved for future platform-admin APIs (Arconia ignore + `ROLE_PLATFORM_ADMIN`). There is no controller there yet.

## Tenants (platform admin)

| Method | Path                               | Role           |
| ------ | ---------------------------------- | -------------- |
| GET    | `/api/tenants`                     | PLATFORM_ADMIN |
| GET    | `/api/tenants/{identifier}`        | PLATFORM_ADMIN |
| POST   | `/api/tenants`                     | PLATFORM_ADMIN |
| PUT    | `/api/tenants/{identifier}/status` | PLATFORM_ADMIN |

```json
{ "identifier": "acme", "name": "ACME Corporation" }
```

Status body: `{ "status": "SUSPENDED" }` (`ACTIVE` / `SUSPENDED` / `DISABLED`).

## Users (tenant projection)

Read-only copy of Keycloak users for the current tenant. Passwords are never stored here.

| Method | Path              | Roles                 |
| ------ | ----------------- | --------------------- |
| GET    | `/api/users`      | TENANT_ADMIN, AUDITOR |
| GET    | `/api/users/{id}` | same                  |

## Customers (tenant-scoped)

| Method | Path                  | Roles                                |
| ------ | --------------------- | ------------------------------------ |
| GET    | `/api/customers`      | TENANT_ADMIN, MANAGER, USER, AUDITOR |
| GET    | `/api/customers/{id}` | same                                 |
| POST   | `/api/customers`      | TENANT_ADMIN, MANAGER                |
| PUT    | `/api/customers/{id}` | TENANT_ADMIN, MANAGER                |
| DELETE | `/api/customers/{id}` | TENANT_ADMIN                         |

```json
{ "name": "Acme Retail", "email": "retail@acme.test", "phone": "+1-555-0101" }
```

## Orders (tenant-scoped)

| Method | Path                        | Roles                                |
| ------ | --------------------------- | ------------------------------------ |
| GET    | `/api/orders`               | TENANT_ADMIN, MANAGER, USER, AUDITOR |
| GET    | `/api/orders/{id}`          | same                                 |
| POST   | `/api/orders`               | TENANT_ADMIN, MANAGER, USER          |
| PUT    | `/api/orders/{id}/confirm`  | TENANT_ADMIN, MANAGER                |
| PUT    | `/api/orders/{id}/cancel`   | TENANT_ADMIN, MANAGER                |
| PUT    | `/api/orders/{id}/complete` | TENANT_ADMIN, MANAGER                |

```json
{ "customerId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1", "amount": 1250.50, "currency": "USD" }
```

States: `CREATED` → `CONFIRMED` \| `CANCELLED`; `CONFIRMED` → `COMPLETED` \| `CANCELLED`.

A settled payment also moves the order to `COMPLETED`. Cancel is rejected if the order already has an in-flight or settled payment.

## Payments (tenant-scoped)

`POST /api/payments` requires header `Idempotency-Key`. Only **CONFIRMED** orders can be paid. One open payment per order.

| Method | Path                              | Roles                                |
| ------ | --------------------------------- | ------------------------------------ |
| GET    | `/api/payments`                   | TENANT_ADMIN, MANAGER, USER, AUDITOR |
| GET    | `/api/payments/{id}`              | same                                 |
| POST   | `/api/payments`                   | TENANT_ADMIN, MANAGER, USER          |
| POST   | `/api/payments/{id}/retry`        | TENANT_ADMIN, MANAGER                |
| POST   | `/api/payments/{id}/cancel`       | TENANT_ADMIN, MANAGER                |
| GET    | `/api/payments/{id}/transactions` | TENANT_ADMIN, MANAGER, USER, AUDITOR |
| GET    | `/api/payments/{id}/ledger`       | same                                 |

```json
{ "orderId": "cccccccc-cccc-cccc-cccc-ccccccccccc2" }
```

PSP simulation uses the last two digits of the amount in cents: `13` timeout, `99` retryable, `07` hard decline, otherwise success. See [PAYMENTS.md](PAYMENTS.md).

## Wallets, settlements, reconciliation

| Method | Path                  | Roles                                |
| ------ | --------------------- | ------------------------------------ |
| GET    | `/api/wallets`        | TENANT_ADMIN, MANAGER, USER, AUDITOR |
| GET    | `/api/settlements`    | same                                 |
| GET    | `/api/reconciliation` | TENANT_ADMIN, MANAGER, AUDITOR       |

## Stats and audit

| Method | Path         | Roles                                          |
| ------ | ------------ | ---------------------------------------------- |
| GET    | `/api/stats` | TENANT_ADMIN, MANAGER                          |
| GET    | `/api/audit` | TENANT_ADMIN, AUDITOR, PLATFORM_ADMIN (global) |

## Actuator

`GET /actuator/health`, `/actuator/info`, `/actuator/prometheus` are public for the local demo. `metrics` and `tenants` are also exposed. Lock them down in production.
