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
    private final PaymentIntentFailureRecorder paymentIntentFailureRecorder;


    PaymentIntentProcessingService(PaymentIntentRepository paymentIntentRepository,
                                   LedgerService ledgerService,
                                   OutboxEventService outboxEventService,
                                   PaymentProcessor paymentProcessor,
                                   PaymentIntentFailureRecorder paymentIntentFailureRecorder) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.ledgerService = ledgerService;
        this.outboxEventService = outboxEventService;
        this.paymentProcessor = paymentProcessor;
        this.paymentIntentFailureRecorder = paymentIntentFailureRecorder;
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

        switch (result) {
            case ProcessorResult.Success(String processorReference) -> {
                paymentIntent.setProcessorReference(processorReference);
                paymentIntent.setStatus(PaymentIntentStateMachine.transition(
                        paymentIntent.getStatus(),
                        PaymentIntentStatus.SUCCEEDED
                ));
                ledgerService.recordDoubleEntry(paymentIntent);

                UUID eventId = UUID.randomUUID();
                PaymentIntentSucceededEvent event = new PaymentIntentSucceededEvent(
                        eventId,
                        paymentIntentId,
                        paymentIntent.getMerchant().getId(),
                        paymentIntent.getAmountMinorUnits(),
                        paymentIntent.getCurrency(),
                        Instant.now(),
                        processorReference
                );
                outboxEventService.recordEvent(
                        eventId,
                        AggregateType.PAYMENT_INTENT,
                        paymentIntentId,
                        EventType.PAYMENT_INTENT_SUCCEEDED,
                        event
                );
            }
            case ProcessorResult.Declined d ->
                    paymentIntentFailureRecorder.applyFailure(paymentIntent, FailureReason.DECLINED, d.reasonCode());
            case ProcessorResult.ProcessorError e ->
                    paymentIntentFailureRecorder.applyFailure(paymentIntent, FailureReason.PROCESSOR_ERROR, e.message());
        }

        paymentIntentRepository.save(paymentIntent);
    }
}
