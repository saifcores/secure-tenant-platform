# Payments (V2)

FinTech payment domain on top of the multi-tenant order API: wallet reservation, simulated PSP, double-entry ledger, settlement, outbox → Kafka, idempotency, retry/timeout, and reconciliation.

## Lifecycle

```
CREATED → AUTHORIZED → CAPTURED → SETTLED
    │           │           │
    └───────────┴───────────┴──► FAILED
CREATED / AUTHORIZED may also go to CANCELLED
```

Wallet movement on the happy path:

1. **Reserve** — `AVAILABLE → RESERVED`
2. **Capture** — `RESERVED → PSP_CLEARING`
3. **Settle** — `PSP_CLEARING → SETTLEMENT`

A hard decline or exhausted retries **releases** the reserve (`RESERVED → AVAILABLE`).

## Simulated PSP

The last two digits of the amount in cents decide the outcome:

| Suffix | Result                         |
| ------ | ------------------------------ |
| `13`   | Timeout (sleep 10s, bounded by `app.payments.psp.timeout`, default 2s) |
| `99`   | Retryable failure              |
| `07`   | Hard decline (`FAILED`)        |
| other  | Success → settle               |

Retryable statuses: `CREATED`, `AUTHORIZED`. Max attempts default **3**, delay **10s**. A scheduled job scans due rows **without** Hibernate `@TenantId` (no request tenant), then re-enters `TenantContext` per payment.

## Idempotency

`POST /api/payments` requires header `Idempotency-Key`.

- Same tenant + key + same `orderId` → replay the stored payment (`201`)
- Same tenant + key + different `orderId` → `409 CONFLICT`
- Unique constraint on `(tenant_id, key)`; the table has **no** `@TenantId` so the lookup is explicit

## Outbox and Kafka

Payment events are written to `outbox_events` in the same transaction as the business change. A publisher polls unpublished rows and sends them to topic `payments.events`.

Kafka is **off** by default (and in tests). Docker profile turns it on (`kafka:9092`). Local broker: `localhost:19092`.

## Isolation

`payments`, `wallets`, `ledger_entries`, `payment_transactions`, and `settlements` use Hibernate `@TenantId`. Cross-tenant reads return **404**. `outbox_events` and `idempotency_keys` store `tenant_id` as a normal column so background jobs can run without a JWT.
