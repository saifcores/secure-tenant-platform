CREATE TABLE audit_events (
    id              UUID PRIMARY KEY,
    tenant_id       VARCHAR(64),
    user_id         VARCHAR(128) NOT NULL,
    action          VARCHAR(64) NOT NULL,
    resource_type   VARCHAR(64) NOT NULL,
    resource_id     VARCHAR(128),
    occurred_at     TIMESTAMPTZ NOT NULL,
    ip_address      VARCHAR(64),
    metadata        JSONB
);

CREATE INDEX idx_audit_tenant ON audit_events (tenant_id);
CREATE INDEX idx_audit_action ON audit_events (action);
CREATE INDEX idx_audit_occurred_at ON audit_events (occurred_at);
