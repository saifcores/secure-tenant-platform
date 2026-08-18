CREATE TABLE orders (
    id              UUID PRIMARY KEY,
    tenant_id       VARCHAR(64) NOT NULL,
    customer_id     UUID NOT NULL REFERENCES customers (id),
    amount          NUMERIC(19, 4) NOT NULL,
    currency        VARCHAR(3) NOT NULL,
    status          VARCHAR(32) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_orders_tenant ON orders (tenant_id);
CREATE INDEX idx_orders_tenant_customer ON orders (tenant_id, customer_id);
CREATE INDEX idx_orders_tenant_status ON orders (tenant_id, status);
