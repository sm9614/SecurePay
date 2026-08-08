package com.pm.paymentplatform.outbox;

import com.pm.paymentplatform.statemachine.Status;

public enum OutboxStatus implements Status {
    PENDING,
    PUBLISHED,
    FAILED,
    DEAD_LETTER
}
