CREATE TABLE refunds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_intent_id UUID NOT NULL REFERENCES payment_intents(id),
    amount_minor_units BIGINT NOT NULL CHECK (amount_minor_units > 0),
    status VARCHAR(255) NOT NULL CHECK (status IN ('CREATED', 'PROCESSING', 'FAILED', 'SUCCEEDED', 'CANCELED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);