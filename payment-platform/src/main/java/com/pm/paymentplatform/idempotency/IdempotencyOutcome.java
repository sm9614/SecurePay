package com.pm.paymentplatform.idempotency;

public enum IdempotencyOutcome {
    CREATED,
    DUPLICATE_PENDING,
    DUPLICATE_COMPLETE
}
