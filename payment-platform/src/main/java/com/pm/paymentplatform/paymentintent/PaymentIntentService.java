package com.pm.paymentplatform.paymentintent;

import com.pm.paymentplatform.fraud.Decision;
import com.pm.paymentplatform.fraud.FraudCheckClient;
import com.pm.paymentplatform.fraud.FraudCheckContext;
import com.pm.paymentplatform.fraud.FraudDecision;
import com.pm.paymentplatform.idempotency.IdempotencyKey;
import com.pm.paymentplatform.merchant.Merchant;
import com.pm.paymentplatform.merchant.MerchantNotFoundException;
import com.pm.paymentplatform.merchant.MerchantRepository;
import com.pm.paymentplatform.payment.ProcessorResult;
import com.pm.paymentplatform.stripe.FailureReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.UUID;

@Service
public class PaymentIntentService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final MerchantRepository merchantRepository;
    private final PaymentIntentProcessingService paymentIntentProcessingService;
    private final FraudCheckContextAssembler assembler;
    private final FraudCheckClient fraudCheckClient;
    private final PaymentIntentFailureRecorder failureRecorder;

    private final Logger log =  LoggerFactory.getLogger(PaymentIntentService.class);

    public PaymentIntentService(PaymentIntentRepository paymentIntentRepository,
                                MerchantRepository merchantRepository,
                                PaymentIntentProcessingService paymentIntentProcessingService,
                                FraudCheckContextAssembler assembler,
                                FraudCheckClient fraudCheckClient,
                                PaymentIntentFailureRecorder failureRecorder) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.merchantRepository = merchantRepository;
        this.paymentIntentProcessingService = paymentIntentProcessingService;
        this.assembler = assembler;
        this.fraudCheckClient = fraudCheckClient;
        this.failureRecorder = failureRecorder;
    }

    public PaymentIntentResponseDTO createPaymentIntent(Long amountMinorUnits,
                                                        Currency currency,
                                                        IdempotencyKey idempotencyKey,
                                                        UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setAmountMinorUnits(amountMinorUnits);
        paymentIntent.setCurrency(currency);
        paymentIntent.setIdempotencyKey(idempotencyKey);
        paymentIntent.setStatus(PaymentIntentStatus.CREATED);
        paymentIntent.setMerchant(merchant);

        paymentIntentRepository.save(paymentIntent);
        return PaymentIntentMapper.toResponseDTO(paymentIntent);
    }

    public PaymentIntentResponseDTO processPaymentIntent(UUID paymentIntentId,
                                              UUID merchantId) {
        PaymentIntentProcessingContext context = paymentIntentProcessingService.beginProcessing(paymentIntentId, merchantId);

        FraudCheckContext fraudCheckContext = assembler.assemble(
                context.paymentIntentId(),
                merchantId,
                context.amountMinorUnits(),
                context.currency());
        log.info("Fraud check requested for transaction {}", fraudCheckContext.transactionId());
        FraudDecision fraudDecision = fraudCheckClient.checkTransaction(fraudCheckContext);
        log.info("Fraud check result for transaction {}: decision={} reasons={}",
                fraudCheckContext.transactionId(), fraudDecision.decision(), fraudDecision.reasonCodes());
        if (fraudDecision.decision() == Decision.BLOCK) {
            failureRecorder.recordFailure(
                    paymentIntentId,
                    FailureReason.DECLINED,
                    String.join(",", fraudDecision.reasonCodes()));
            return loadResponse(paymentIntentId);

        }
        if (fraudDecision.decision() == Decision.REVIEW) {
            log.warn("Fraud REVIEW treated as ALLOW for paymentIntent={} reasons={}",
                    paymentIntentId, fraudDecision.reasonCodes());
        }

        ProcessorResult result = paymentIntentProcessingService.executeCharge(context);
        paymentIntentProcessingService.completeProcessing(paymentIntentId, result);
        return loadResponse(paymentIntentId);
    }

    private PaymentIntentResponseDTO loadResponse(UUID paymentIntentId) {
        PaymentIntent paymentIntent = paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new PaymentIntentNotFoundException(paymentIntentId));
        return PaymentIntentMapper.toResponseDTO(paymentIntent);
    }
}
