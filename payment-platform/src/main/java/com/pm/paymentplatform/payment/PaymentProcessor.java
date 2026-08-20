package com.pm.paymentplatform.payment;

public interface PaymentProcessor {

    ProcessorResult processRefund(String paymentIntentReference,
                                  Long amountMinorUnits,
                                  String idempotencyKey
    );
}
