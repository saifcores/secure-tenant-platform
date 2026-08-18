INSERT INTO tenants (id, identifier, name, status, created_at, updated_at) VALUES
    ('11111111-1111-1111-1111-111111111111', 'acme',    'ACME Corporation', 'ACTIVE',    TIMESTAMPTZ '2026-01-01T00:00:00Z', TIMESTAMPTZ '2026-01-01T00:00:00Z'),
    ('22222222-2222-2222-2222-222222222222', 'globex',  'Globex Corporation', 'ACTIVE',  TIMESTAMPTZ '2026-01-01T00:00:00Z', TIMESTAMPTZ '2026-01-01T00:00:00Z'),
    ('33333333-3333-3333-3333-333333333333', 'fincorp', 'FinCorp Holdings', 'ACTIVE',    TIMESTAMPTZ '2026-01-01T00:00:00Z', TIMESTAMPTZ '2026-01-01T00:00:00Z');

INSERT INTO customers (id, tenant_id, name, email, phone, status, created_at, updated_at) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'acme',   'Acme Retail',  'retail@acme.test',  '+1-555-0101', 'ACTIVE', TIMESTAMPTZ '2026-01-02T00:00:00Z', TIMESTAMPTZ '2026-01-02T00:00:00Z'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'acme',   'Acme Logistics','logistics@acme.test','+1-555-0102', 'ACTIVE', TIMESTAMPTZ '2026-01-02T00:00:00Z', TIMESTAMPTZ '2026-01-02T00:00:00Z'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1', 'globex', 'Globex Energy','energy@globex.test','+1-555-0201', 'ACTIVE', TIMESTAMPTZ '2026-01-02T00:00:00Z', TIMESTAMPTZ '2026-01-02T00:00:00Z');

INSERT INTO orders (id, tenant_id, customer_id, amount, currency, status, created_at, updated_at) VALUES
    ('cccccccc-cccc-cccc-cccc-ccccccccccc1', 'acme',   'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 1250.50, 'USD', 'CREATED',   TIMESTAMPTZ '2026-01-03T00:00:00Z', TIMESTAMPTZ '2026-01-03T00:00:00Z'),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc2', 'acme',   'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1',  890.00, 'USD', 'CONFIRMED', TIMESTAMPTZ '2026-01-03T00:00:00Z', TIMESTAMPTZ '2026-01-03T00:00:00Z'),
    ('dddddddd-dddd-dddd-dddd-ddddddddddd1', 'globex', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1', 9999.99, 'USD', 'CREATED',   TIMESTAMPTZ '2026-01-03T00:00:00Z', TIMESTAMPTZ '2026-01-03T00:00:00Z');
