CREATE TABLE wallets (
    id                  UUID PRIMARY KEY,
    tenant_id           VARCHAR(64) NOT NULL,
    currency            VARCHAR(3) NOT NULL,
    available_balance   NUMERIC(19, 4) NOT NULL,
    reserved_balance    NUMERIC(19, 4) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_wallets_tenant_currency UNIQUE (tenant_id, currency),
    CONSTRAINT chk_wallets_available CHECK (available_balance >= 0),
    CONSTRAINT chk_wallets_reserved CHECK (reserved_balance >= 0)
);

CREATE INDEX idx_wallets_tenant ON wallets (tenant_id);

CREATE TABLE payments (
    id                  UUID PRIMARY KEY,
    tenant_id           VARCHAR(64) NOT NULL,
    order_id            UUID REFERENCES orders (id),
    wallet_id           UUID NOT NULL REFERENCES wallets (id),
    amount              NUMERIC(19, 4) NOT NULL,
    currency            VARCHAR(3) NOT NULL,
    status              VARCHAR(32) NOT NULL,
    failure_reason      VARCHAR(255),
    attempt_count       INTEGER NOT NULL DEFAULT 0,
    next_retry_at       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_payments_tenant ON payments (tenant_id);
CREATE INDEX idx_payments_tenant_status ON payments (tenant_id, status);
CREATE INDEX idx_payments_retry ON payments (status, next_retry_at);

CREATE TABLE payment_transactions (
    id                  UUID PRIMARY KEY,
    tenant_id           VARCHAR(64) NOT NULL,
    payment_id          UUID NOT NULL REFERENCES payments (id),
    attempt             INTEGER NOT NULL,
    psp_reference       VARCHAR(128),
    status              VARCHAR(32) NOT NULL,
    error_code          VARCHAR(64),
    error_message       VARCHAR(512),
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_payment_tx_payment ON payment_transactions (payment_id);

CREATE TABLE ledger_entries (
    id                  UUID PRIMARY KEY,
    tenant_id           VARCHAR(64) NOT NULL,
    payment_id          UUID REFERENCES payments (id),
    wallet_id           UUID REFERENCES wallets (id),
    account             VARCHAR(64) NOT NULL,
    direction           VARCHAR(8) NOT NULL,
    amount              NUMERIC(19, 4) NOT NULL,
    currency            VARCHAR(3) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_ledger_tenant ON ledger_entries (tenant_id);
CREATE INDEX idx_ledger_payment ON ledger_entries (payment_id);

CREATE TABLE settlements (
    id                  UUID PRIMARY KEY,
    tenant_id           VARCHAR(64) NOT NULL,
    payment_id          UUID NOT NULL REFERENCES payments (id),
    amount              NUMERIC(19, 4) NOT NULL,
    currency            VARCHAR(3) NOT NULL,
    status              VARCHAR(32) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_settlements_tenant ON settlements (tenant_id);

CREATE TABLE outbox_events (
    id                  UUID PRIMARY KEY,
    tenant_id           VARCHAR(64),
    aggregate_type      VARCHAR(64) NOT NULL,
    aggregate_id        VARCHAR(128) NOT NULL,
    event_type          VARCHAR(64) NOT NULL,
    payload             JSONB NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL,
    published_at        TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unpublished ON outbox_events (created_at) WHERE published_at IS NULL;

CREATE TABLE idempotency_keys (
    id                  UUID PRIMARY KEY,
    tenant_id           VARCHAR(64) NOT NULL,
    key                 VARCHAR(128) NOT NULL,
    request_hash        VARCHAR(64) NOT NULL,
    response_body       TEXT,
    http_status         INTEGER,
    created_at          TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_idempotency_tenant_key UNIQUE (tenant_id, key)
);
