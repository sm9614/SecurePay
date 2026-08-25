package com.pm.paymentplatform.paymentintent;

import com.pm.paymentplatform.ledger.LedgerService;
import com.pm.paymentplatform.merchant.MerchantRepository;
import com.pm.paymentplatform.outbox.OutboxEventService;
import com.pm.paymentplatform.payment.PaymentProcessor;
import com.pm.paymentplatform.payment.ProcessorResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class PaymentIntentProcessingService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentProcessor paymentProcessor;
    private final LedgerService ledgerService;

    PaymentIntentProcessingService(PaymentIntentRepository paymentIntentRepository,
                                   MerchantRepository merchantRepository,
                                   OutboxEventService outboxEventService,
                                   PaymentProcessor paymentProcessor, LedgerService ledgerService) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.paymentProcessor = paymentProcessor;
        this.ledgerService = ledgerService;
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

        if (Objects.requireNonNull(result) instanceof ProcessorResult.Success(String processorRefence)) {
            paymentIntent.setProcessorReference(processorRefence);
            paymentIntent.setStatus(PaymentIntentStateMachine.transition(
                    paymentIntent.getStatus(),
                    PaymentIntentStatus.SUCCEEDED
            ));
            ledgerService.recordDoubleEntry(paymentIntent);
        } else {
            paymentIntent.setStatus(PaymentIntentStateMachine.transition(
                    paymentIntent.getStatus(),
                    PaymentIntentStatus.FAILED
            ));
        }
        paymentIntentRepository.save(paymentIntent);
    }

}
