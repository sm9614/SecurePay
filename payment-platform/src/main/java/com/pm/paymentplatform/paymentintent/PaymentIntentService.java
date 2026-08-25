package com.pm.paymentplatform.paymentintent;

import com.pm.paymentplatform.idempotency.IdempotencyKey;
import com.pm.paymentplatform.merchant.Merchant;
import com.pm.paymentplatform.merchant.MerchantNotFoundException;
import com.pm.paymentplatform.merchant.MerchantRepository;
import com.pm.paymentplatform.payment.ProcessorResult;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.UUID;

@Service
public class PaymentIntentService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final MerchantRepository merchantRepository;
    private final PaymentIntentProcessingService paymentIntentProcessingService;

    public PaymentIntentService(PaymentIntentRepository paymentIntentRepository,
                                MerchantRepository merchantRepository,
                                PaymentIntentProcessingService paymentIntentProcessingService) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.merchantRepository = merchantRepository;
        this.paymentIntentProcessingService = paymentIntentProcessingService;
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
        ProcessorResult result = paymentIntentProcessingService.executeCharge(context);
        paymentIntentProcessingService.completeProcessing(paymentIntentId, result);

        PaymentIntent paymentIntent = paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new PaymentIntentNotFoundException(paymentIntentId));

        return PaymentIntentMapper.toResponseDTO(paymentIntent);
    }

}
