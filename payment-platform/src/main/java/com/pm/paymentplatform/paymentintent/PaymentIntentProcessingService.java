package com.pm.paymentplatform.paymentintent;

import com.pm.paymentplatform.ledger.LedgerService;
import com.pm.paymentplatform.messaging.EventType;
import com.pm.paymentplatform.outbox.AggregateType;
import com.pm.paymentplatform.outbox.OutboxEventService;
import com.pm.paymentplatform.payment.PaymentProcessor;
import com.pm.paymentplatform.payment.ProcessorResult;
import com.pm.paymentplatform.stripe.FailureReason;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Service
public class PaymentIntentProcessingService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final LedgerService ledgerService;
    private final OutboxEventService outboxEventService;
    private final PaymentProcessor paymentProcessor;


    PaymentIntentProcessingService(PaymentIntentRepository paymentIntentRepository,
                                   LedgerService ledgerService,
                                   OutboxEventService outboxEventService,
                                   PaymentProcessor paymentProcessor) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.ledgerService = ledgerService;
        this.outboxEventService = outboxEventService;
        this.paymentProcessor = paymentProcessor;
    }

    @Transactional
    public PaymentIntentProcessingContext beginProcessing(UUID paymentIntentId,
                                                          UUID merchantId) {
        PaymentIntent paymentIntent = paymentIntentRepository.getPaymentIntentByIdWithLock(paymentIntentId)
                .orElseThrow(() -> new PaymentIntentNotFoundException(paymentIntentId));

        if (!paymentIntent.getMerchant().getId().equals(merchantId)) {
            throw new PaymentIntentNotFoundException(paymentIntentId);
        }

        paymentIntent.setStatus(PaymentIntentStateMachine.transition(
                paymentIntent.getStatus(),
                PaymentIntentStatus.PROCESSING));

        paymentIntentRepository.save(paymentIntent);

        return new PaymentIntentProcessingContext(paymentIntentId,
                paymentIntent.getAmountMinorUnits(),
                paymentIntent.getCurrency());
    }

    public ProcessorResult executeCharge(PaymentIntentProcessingContext context) {
        String idempotencyKey = context.paymentIntentId().toString();

        return paymentProcessor.processPayment(
                context.amountMinorUnits(),
                context.currency(),
                idempotencyKey
        );
    }

    @Transactional
    public void completeProcessing(UUID paymentIntentId, ProcessorResult result) {
        PaymentIntent paymentIntent = paymentIntentRepository.getPaymentIntentByIdWithLock(paymentIntentId)
                .orElseThrow(() -> new PaymentIntentNotFoundException(paymentIntentId));

        UUID eventId = UUID.randomUUID();
        UUID merchantId = paymentIntent.getMerchant().getId();
        Long amountMinorUnits = paymentIntent.getAmountMinorUnits();
        Currency currency = paymentIntent.getCurrency();
        Instant occurredAt = Instant.now();

        EventType eventType;
        Object event;

        switch (result) {
            case ProcessorResult.Success(String processorReference) -> {
                paymentIntent.setProcessorReference(processorReference);
                paymentIntent.setStatus(PaymentIntentStateMachine.transition(
                        paymentIntent.getStatus(),
                        PaymentIntentStatus.SUCCEEDED
                ));
                ledgerService.recordDoubleEntry(paymentIntent);
                eventType = EventType.PAYMENT_INTENT_SUCCEEDED;
                event = new PaymentIntentSucceededEvent(
                        eventId,
                        paymentIntentId,
                        merchantId,
                        amountMinorUnits,
                        currency,
                        occurredAt,
                        processorReference
                );
            }

            case ProcessorResult.Declined(String reasonCode, String message) -> {
                paymentIntent.setStatus(PaymentIntentStateMachine.transition(
                        paymentIntent.getStatus(),
                        PaymentIntentStatus.FAILED
                ));
                eventType = EventType.PAYMENT_INTENT_FAILED;
                event = new PaymentIntentFailedEvent(
                        eventId,
                        paymentIntentId,
                        merchantId,
                        amountMinorUnits,
                        currency,
                        occurredAt,
                        FailureReason.DECLINED,
                        reasonCode
                );
            }
            case ProcessorResult.ProcessorError(String message) -> {
                paymentIntent.setStatus(PaymentIntentStateMachine.transition(
                        paymentIntent.getStatus(),
                        PaymentIntentStatus.FAILED
                ));
                eventType = EventType.PAYMENT_INTENT_FAILED;
                event = new PaymentIntentFailedEvent(
                        eventId,
                        paymentIntentId,
                        merchantId,
                        amountMinorUnits,
                        currency,
                        occurredAt,
                        FailureReason.PROCESSOR_ERROR,
                        message
                );
            }
        }
        outboxEventService.recordEvent(
                eventId,
                AggregateType.PAYMENT_INTENT,
                paymentIntentId,
                eventType,
                event
        );
        paymentIntentRepository.save(paymentIntent);
    }
}
