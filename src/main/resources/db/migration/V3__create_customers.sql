CREATE TABLE customers (
    id              UUID PRIMARY KEY,
    tenant_id       VARCHAR(64) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    phone           VARCHAR(64),
    status          VARCHAR(32) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_customers_tenant_email UNIQUE (tenant_id, email)
);

CREATE INDEX idx_customers_tenant ON customers (tenant_id);
