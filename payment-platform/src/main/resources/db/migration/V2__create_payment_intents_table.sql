CREATE TABLE payment_intents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    amount_minor_units BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(255) NOT NULL CHECK (status IN ('CREATED', 'PROCESSING', 'FAILED', 'SUCCEEDED')),
    idempotency_key_id UUID NOT NULL UNIQUE  REFERENCES idempotency_keys(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
