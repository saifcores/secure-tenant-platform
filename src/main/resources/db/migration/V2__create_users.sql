CREATE TABLE users (
    id              UUID PRIMARY KEY,
    tenant_id       VARCHAR(64) NOT NULL,
    username        VARCHAR(128) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    role            VARCHAR(64) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_users_tenant_username UNIQUE (tenant_id, username),
    CONSTRAINT uk_users_tenant_email UNIQUE (tenant_id, email)
);

CREATE INDEX idx_users_tenant ON users (tenant_id);
