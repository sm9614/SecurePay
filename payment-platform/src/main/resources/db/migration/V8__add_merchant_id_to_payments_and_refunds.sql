ALTER TABLE payment_intents
    ADD COLUMN merchant_id UUID NOT NULL REFERENCES merchants(id);

ALTER TABLE refunds
    ADD COLUMN merchant_id UUID NOT NULL REFERENCES merchants(id);

