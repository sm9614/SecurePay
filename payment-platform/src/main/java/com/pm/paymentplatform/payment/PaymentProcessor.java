package com.pm.paymentplatform.payment;

import java.util.Currency;

public interface PaymentProcessor {

    ProcessorResult processRefund(String paymentIntentReference,
                                  Long amountMinorUnits,
                                  String idempotencyKey
    );

    ProcessorResult processPayment(Long amountMinorUnits,
                                   Currency currency,
                                   String idempotencyKey
    );
}
