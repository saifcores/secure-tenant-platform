INSERT INTO users (id, tenant_id, username, email, role, created_at, updated_at) VALUES
    ('44444444-4444-4444-4444-444444444441', 'acme',    'alice', 'alice@acme.test',   'TENANT_ADMIN', TIMESTAMPTZ '2026-01-01T00:00:00Z', TIMESTAMPTZ '2026-01-01T00:00:00Z'),
    ('44444444-4444-4444-4444-444444444442', 'acme',    'bob',   'bob@acme.test',     'USER',         TIMESTAMPTZ '2026-01-01T00:00:00Z', TIMESTAMPTZ '2026-01-01T00:00:00Z'),
    ('44444444-4444-4444-4444-444444444443', 'acme',    'carol', 'carol@acme.test',   'MANAGER',      TIMESTAMPTZ '2026-01-01T00:00:00Z', TIMESTAMPTZ '2026-01-01T00:00:00Z'),
    ('44444444-4444-4444-4444-444444444444', 'acme',    'dave',  'dave@acme.test',    'AUDITOR',      TIMESTAMPTZ '2026-01-01T00:00:00Z', TIMESTAMPTZ '2026-01-01T00:00:00Z'),
    ('55555555-5555-5555-5555-555555555551', 'globex',  'john',  'john@globex.test',  'TENANT_ADMIN', TIMESTAMPTZ '2026-01-01T00:00:00Z', TIMESTAMPTZ '2026-01-01T00:00:00Z'),
    ('55555555-5555-5555-5555-555555555552', 'globex',  'mike',  'mike@globex.test',  'USER',         TIMESTAMPTZ '2026-01-01T00:00:00Z', TIMESTAMPTZ '2026-01-01T00:00:00Z'),
    ('66666666-6666-6666-6666-666666666661', 'fincorp', 'nina',  'nina@fincorp.test', 'TENANT_ADMIN', TIMESTAMPTZ '2026-01-01T00:00:00Z', TIMESTAMPTZ '2026-01-01T00:00:00Z'),
    ('66666666-6666-6666-6666-666666666662', 'fincorp', 'paul',  'paul@fincorp.test', 'USER',         TIMESTAMPTZ '2026-01-01T00:00:00Z', TIMESTAMPTZ '2026-01-01T00:00:00Z');

INSERT INTO customers (id, tenant_id, name, email, phone, status, created_at, updated_at) VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2', 'fincorp', 'FinCorp Treasury', 'treasury@fincorp.test', '+1-555-0301', 'ACTIVE', TIMESTAMPTZ '2026-01-02T00:00:00Z', TIMESTAMPTZ '2026-01-02T00:00:00Z');
