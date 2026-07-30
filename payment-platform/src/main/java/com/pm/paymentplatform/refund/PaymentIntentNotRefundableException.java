package com.pm.paymentplatform.refund;

import com.pm.paymentplatform.paymentintent.PaymentIntentStatus;

public class PaymentIntentNotRefundableException extends RuntimeException {

    private final PaymentIntentStatus paymentIntentStatus;

    public PaymentIntentNotRefundableException(PaymentIntentStatus paymentIntentStatus) {
        super("Cannot refund payment with status: " + paymentIntentStatus);

        this.paymentIntentStatus = paymentIntentStatus;
    }

    public PaymentIntentStatus getPaymentIntentStatus() {
        return paymentIntentStatus;
    }
}
