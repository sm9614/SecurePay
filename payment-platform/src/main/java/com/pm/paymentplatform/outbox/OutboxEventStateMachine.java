package com.pm.paymentplatform.outbox;

import com.pm.paymentplatform.statemachine.InvalidStateTransitionException;

import java.util.Map;
import java.util.Set;

public class OutboxEventStateMachine {

    private static final Map<OutboxStatus, Set<OutboxStatus>> STATE_MACHINE = Map.of(
            OutboxStatus.PENDING, Set.of(OutboxStatus.PUBLISHED, OutboxStatus.FAILED),
            OutboxStatus.FAILED, Set.of(OutboxStatus.PENDING, OutboxStatus.DEAD_LETTER),
            OutboxStatus.PUBLISHED, Set.of(),
            OutboxStatus.DEAD_LETTER, Set.of()
    );

    private OutboxEventStateMachine() {

    }

    public static boolean isValidTransition(OutboxStatus status, OutboxStatus next) {
        return STATE_MACHINE.getOrDefault(status, Set.of()).contains(next);
    }

    public static OutboxStatus transition(OutboxStatus status, OutboxStatus next) {
        if (!isValidTransition(status, next)) {
            throw new InvalidStateTransitionException(status, next);
        }

        return next;
    }
}
