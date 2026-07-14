package com.pm.paymentplatform.idempotency;

public class IdempotencyKeyNotFoundException extends RuntimeException {
    public IdempotencyKeyNotFoundException(String message) {
        super(message);
    }
}
