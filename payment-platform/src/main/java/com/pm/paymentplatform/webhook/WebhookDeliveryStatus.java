package com.pm.paymentplatform.webhook;

import com.pm.paymentplatform.statemachine.Status;

public enum WebhookDeliveryStatus implements Status {
    PENDING,
    DELIVERED,
    FAILED,
    DEAD_LETTER
}
