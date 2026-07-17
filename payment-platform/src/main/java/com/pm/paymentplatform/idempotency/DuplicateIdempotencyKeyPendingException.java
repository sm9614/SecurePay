package com.pm.paymentplatform.idempotency;

public class DuplicateIdempotencyKeyPendingException extends RuntimeException {
    public DuplicateIdempotencyKeyPendingException(String message) {
        super(message);
    }
}
