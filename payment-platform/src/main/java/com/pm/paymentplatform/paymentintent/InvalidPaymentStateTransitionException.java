package com.pm.paymentplatform.paymentintent;

public class InvalidPaymentStateTransitionException extends RuntimeException {

    private final PaymentIntentStatus fromStatus;
    private final PaymentIntentStatus toStatus;

    public InvalidPaymentStateTransitionException(PaymentIntentStatus fromStatus, PaymentIntentStatus toStatus) {
        super("Cannot transition from " + fromStatus + " to " + toStatus);

        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }

    public PaymentIntentStatus getFromStatus() {
        return fromStatus;
    }

    public PaymentIntentStatus getToStatus() {
        return toStatus;
    }
}
