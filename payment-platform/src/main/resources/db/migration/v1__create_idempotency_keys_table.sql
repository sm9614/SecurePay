CREATE TABLE idempotency_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    operation_type VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    response_status INTEGER,
    response_body TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);