package com.pm.paymentplatform.statemachine;

public class InvalidStateTransitionException extends RuntimeException {

    private final Status fromStatus;
    private final Status toStatus;

    public InvalidStateTransitionException(Status fromStatus, Status toStatus) {
        super("Cannot transition from " + fromStatus + " to " + toStatus);

        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }

    public Status getFromStatus() {
        return fromStatus;
    }

    public Status getToStatus() {
        return toStatus;
    }
}
