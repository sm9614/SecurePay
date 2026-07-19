package com.pm.paymentplatform.refund;

import com.pm.paymentplatform.statemachine.Status;

public enum RefundStatus implements Status {
    CREATED,
    PROCESSING,
    FAILED,
    SUCCEEDED,
    CANCELED
}
