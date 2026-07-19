package com.pm.paymentplatform.refund;

import com.pm.paymentplatform.statemachine.InvalidStateTransitionException;

import java.util.Map;
import java.util.Set;

public class RefundStateMachine {
    private static final Map<RefundStatus, Set<RefundStatus>> STATE_MACHINE = Map.of(
            RefundStatus.CREATED, Set.of(RefundStatus.PROCESSING, RefundStatus.CANCELED),
            RefundStatus.PROCESSING, Set.of(RefundStatus.FAILED, RefundStatus.SUCCEEDED, RefundStatus.CANCELED),
            RefundStatus.FAILED, Set.of(RefundStatus.PROCESSING, RefundStatus.CANCELED),
            RefundStatus.SUCCEEDED, Set.of(),
            RefundStatus.CANCELED, Set.of()
    );

    private RefundStateMachine() {

    }

    public static boolean isValidTransition(RefundStatus status, RefundStatus next) {
        return STATE_MACHINE.getOrDefault(status, Set.of()).contains(next);
    }

    public static RefundStatus transition(RefundStatus status, RefundStatus next) {
        if (!isValidTransition(status, next)) {
            throw new InvalidStateTransitionException(status, next);
        }

        return next;
    }
}
