package com.pm.paymentplatform.paymentintent;

import com.pm.paymentplatform.messaging.EventType;
import com.pm.paymentplatform.outbox.AggregateType;
import com.pm.paymentplatform.outbox.OutboxEventService;
import com.pm.paymentplatform.stripe.FailureReason;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentIntentFailureRecorder {

    private final PaymentIntentRepository paymentIntentRepository;
    private final OutboxEventService outboxEventService;

    public PaymentIntentFailureRecorder(PaymentIntentRepository paymentIntentRepository,
                                        OutboxEventService outboxEventService) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.outboxEventService = outboxEventService;
    }

    @Transactional
    public void recordFailure(UUID paymentIntentId,
                              FailureReason reason,
                              String reasonDetail) {
        PaymentIntent paymentIntent = paymentIntentRepository.getPaymentIntentByIdWithLock(paymentIntentId)
                .orElseThrow(() -> new PaymentIntentNotFoundException(paymentIntentId));
        applyFailure(paymentIntent, reason, reasonDetail);
        paymentIntentRepository.save(paymentIntent);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void applyFailure(PaymentIntent paymentIntent, FailureReason reason, String reasonDetail) {
        paymentIntent.setStatus(PaymentIntentStateMachine.transition(
                paymentIntent.getStatus(),
                PaymentIntentStatus.FAILED
        ));

        UUID eventId = UUID.randomUUID();
        PaymentIntentFailedEvent event = new PaymentIntentFailedEvent(
                eventId,
                paymentIntent.getId(),
                paymentIntent.getMerchant().getId(),
                paymentIntent.getAmountMinorUnits(),
                paymentIntent.getCurrency(),
                Instant.now(),
                reason,
                reasonDetail
        );

        outboxEventService.recordEvent(
                eventId,
                AggregateType.PAYMENT_INTENT,
                paymentIntent.getId(),
                EventType.PAYMENT_INTENT_FAILED,
                event
        );
    }
}
