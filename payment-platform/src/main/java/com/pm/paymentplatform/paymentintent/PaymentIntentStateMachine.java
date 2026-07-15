package com.pm.paymentplatform.paymentintent;

import java.util.Map;
import java.util.Set;

public class PaymentIntentStateMachine {
    private static final Map<PaymentIntentStatus, Set<PaymentIntentStatus>> STATE_MACHINE = Map.of(
            PaymentIntentStatus.CREATED, Set.of(PaymentIntentStatus.PROCESSING),
            PaymentIntentStatus.PROCESSING, Set.of(PaymentIntentStatus.FAILED, PaymentIntentStatus.SUCCEEDED),
            PaymentIntentStatus.FAILED, Set.of(PaymentIntentStatus.PROCESSING),
            PaymentIntentStatus.SUCCEEDED, Set.of()
    );

    private PaymentIntentStateMachine() {

    }

    public static boolean isValidTransition(PaymentIntentStatus status, PaymentIntentStatus next) {
        return STATE_MACHINE.getOrDefault(status, Set.of()).contains(next);
    }

    public static PaymentIntentStatus transition(PaymentIntentStatus status, PaymentIntentStatus next) {
        if (!isValidTransition(status, next)) {
            throw new IllegalStateException("Cannot transition from " + status + " to " + next);
        }

        return next;
    }
}
