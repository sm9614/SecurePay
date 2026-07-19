package com.pm.paymentplatform.paymentintent;

import com.pm.paymentplatform.statemachine.Status;

public enum PaymentIntentStatus implements Status {
    CREATED,
    PROCESSING,
    FAILED,
    SUCCEEDED
}
