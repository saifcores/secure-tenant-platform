CREATE TABLE tenants (
    id              UUID PRIMARY KEY,
    identifier      VARCHAR(64) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    status          VARCHAR(32) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_tenants_status ON tenants (status);
