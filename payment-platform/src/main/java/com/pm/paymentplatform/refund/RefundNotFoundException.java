package com.pm.paymentplatform.refund;

import java.util.UUID;

public class RefundNotFoundException extends RuntimeException {

    private final UUID id;

    public RefundNotFoundException(UUID id) {
        super("Refund with ID: " + id + " not found");
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
