package com.pm.paymentplatform.refund;

import com.pm.paymentplatform.merchant.Merchant;
import com.pm.paymentplatform.merchant.MerchantNotFoundException;
import com.pm.paymentplatform.merchant.MerchantRepository;
import com.pm.paymentplatform.messaging.EventType;
import com.pm.paymentplatform.outbox.AggregateType;
import com.pm.paymentplatform.outbox.OutboxEventService;
import com.pm.paymentplatform.payment.PaymentProcessor;
import com.pm.paymentplatform.payment.ProcessorResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

@Service
public class RefundProcessingService {

    private final RefundRepository refundRepository;
    private final MerchantRepository merchantRepository;
    private final OutboxEventService outboxEventService;
    private final PaymentProcessor paymentProcessor;

    public RefundProcessingService(RefundRepository refundRepository,
                                   MerchantRepository merchantRepository,
                                   OutboxEventService outboxEventService,
                                   PaymentProcessor paymentProcessor) {
        this.refundRepository = refundRepository;
        this.merchantRepository = merchantRepository;
        this.outboxEventService = outboxEventService;
        this.paymentProcessor = paymentProcessor;
    }

    @Transactional
    public RefundProcessingContext beginProcessing(UUID refundId,
                                                   UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

        Refund refund = refundRepository.getRefundByIdWithLock(refundId)
                .orElseThrow(() -> new RefundNotFoundException(refundId));

        if (!refund.getMerchant().getId().equals(merchant.getId())) {
            throw new RefundNotFoundException(refundId);
        }

        refund.setStatus(RefundStateMachine.transition(
                refund.getStatus(),
                RefundStatus.PROCESSING));

        String processorReference = refund.getPaymentIntent().getProcessorReference();
        Long amountMinorUnits = refund.getAmountMinorUnits();

        return new RefundProcessingContext(refundId, processorReference, amountMinorUnits);
    }

    public ProcessorResult executeRefund (RefundProcessingContext context) {
        String idempotencyKey = context.refundId().toString();

        return paymentProcessor.processRefund(
                context.processorReference(),
                context.amountMinorUnits(),
                idempotencyKey
        );
    }

    @Transactional
    public void completeProcessing(UUID refundId, ProcessorResult result) {

        Refund refund = refundRepository.getRefundByIdWithLock(refundId)
                .orElseThrow(() -> new RefundNotFoundException(refundId));

        UUID eventId = UUID.randomUUID();
        UUID paymentIntentId = refund.getPaymentIntent().getId();
        UUID merchantId = refund.getMerchant().getId();
        Long amountMinorUnits = refund.getAmountMinorUnits();
        Currency currency = refund.getPaymentIntent().getCurrency();
        Instant occurredAt = Instant.now();

        EventType eventType;
        Object event;

        if (Objects.requireNonNull(result) instanceof ProcessorResult.Success(String processorReference)) {
            refund.setProcessorReference(processorReference);
            refund.setStatus(RefundStateMachine.transition(
                    refund.getStatus(),
                    RefundStatus.SUCCEEDED
            ));
            eventType = EventType.REFUND_SUCCEEDED;
            event = new RefundSucceededEvent(
                    eventId,
                    refundId,
                    paymentIntentId,
                    merchantId,
                    amountMinorUnits,
                    currency,
                    occurredAt,
                    processorReference
            );

        } else {
            refund.setStatus(RefundStateMachine.transition(
                    refund.getStatus(),
                    RefundStatus.FAILED
            ));
            eventType = EventType.REFUND_FAILED;
            event = new RefundFailedEvent(
                    eventId,
                    refundId,
                    paymentIntentId,
                    merchantId,
                    amountMinorUnits,
                    currency,
                    occurredAt
            );
        }
        outboxEventService.recordEvent(
                eventId,
                AggregateType.REFUND,
                refundId,
                eventType,
                event
        );
        refundRepository.save(refund);
    }
}
