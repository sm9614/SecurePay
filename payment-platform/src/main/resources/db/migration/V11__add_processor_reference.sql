ALTER TABLE payment_intents
    ADD COLUMN processor_reference VARCHAR(255);

ALTER TABLE refunds
    ADD COLUMN processor_reference VARCHAR(255);
