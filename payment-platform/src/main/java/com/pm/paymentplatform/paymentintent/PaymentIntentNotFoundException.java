package com.pm.paymentplatform.paymentintent;

import java.util.UUID;

public class PaymentIntentNotFoundException extends RuntimeException {
    private final UUID id;

    public PaymentIntentNotFoundException(UUID id) {
        super("Payment with ID: " + id + "not found");
        this.id = id;
    }

    public UUID getId() {
        return id;
    }


}
