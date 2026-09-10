CREATE TABLE webhook_endpoints
(
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id            UUID        NOT NULL REFERENCES merchants (id),
    url                    TEXT        NOT NULL,
    signing_secret         TEXT        NOT NULL,
    subscribed_event_types TEXT[] NOT NULL,
    is_active              BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_webhook_endpoints_merchant_id ON webhook_endpoints (merchant_id);

CREATE TABLE webhook_deliveries
(
    id                  UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    webhook_endpoint_id UUID        NOT NULL REFERENCES webhook_endpoints (id),
    event_id            UUID        NOT NULL,
    event_type          TEXT        NOT NULL,
    payload             TEXT        NOT NULL,
    status              TEXT        NOT NULL DEFAULT 'PENDING',
    attempt_count       INT         NOT NULL DEFAULT 0,
    next_attempt_at     TIMESTAMPTZ NOT NULL,
    last_response_code  TEXT,
    last_response_body  TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_webhook_deliveries_endpoint_id
    ON webhook_deliveries (webhook_endpoint_id);

CREATE INDEX idx_webhook_deliveries_status_next_attempt
    ON webhook_deliveries (status, next_attempt_at);