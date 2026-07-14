package com.pm.paymentplatform.exception;

import java.time.Instant;
import java.util.Map;

public class ErrorResponse {

    private final String message;
    private final Map<String, String> errors;
    private final Instant timestamp;

    public ErrorResponse(String message, Map<String, String> errors, Instant timestamp) {
        this.message = message;
        this.errors = errors;
        this.timestamp = timestamp;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
