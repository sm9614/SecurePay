package com.pm.paymentplatform.paymentintent;

import com.pm.paymentplatform.idempotency.IdempotencyKey;
import com.pm.paymentplatform.merchant.Merchant;
import com.pm.paymentplatform.merchant.MerchantNotFoundException;
import com.pm.paymentplatform.merchant.MerchantRepository;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentIntentService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final MerchantRepository merchantRepository;

    public PaymentIntentService(PaymentIntentRepository paymentIntentRepository,
                                MerchantRepository merchantRepository) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.merchantRepository = merchantRepository;
    }

    public PaymentIntentResponseDTO createPaymentIntent(Long amountMinorUnits,
                                                        Currency currency,
                                                        IdempotencyKey idempotencyKey,
                                                        UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow( () -> new MerchantNotFoundException(merchantId));

        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setAmountMinorUnits(amountMinorUnits);
        paymentIntent.setCurrency(currency);
        paymentIntent.setIdempotencyKey(idempotencyKey);
        paymentIntent.setStatus(PaymentIntentStatus.CREATED);
        paymentIntent.setMerchant(merchant);

        paymentIntentRepository.save(paymentIntent);
        return PaymentIntentMapper.toResponseDTO(paymentIntent);
    }

    public PaymentIntent processPaymentIntent(UUID paymentIntentId,
                                              UUID merchantId) {
        PaymentIntent paymentIntent = paymentIntentRepository.getPaymentIntentById(paymentIntentId)
                .orElseThrow(() -> new PaymentIntentNotFoundException(paymentIntentId));

        if (!paymentIntent.getMerchant().getId().equals(merchantId)) {
            throw new PaymentIntentNotFoundException(paymentIntentId);
        }

        paymentIntent.setStatus(PaymentIntentStateMachine.transition(
                        paymentIntent.getStatus(),
                        PaymentIntentStatus.PROCESSING));

        paymentIntentRepository.save(paymentIntent);

        return paymentIntent;
    }

    public PaymentIntentResponseDTO completePaymentIntent(PaymentIntent paymentIntent) {
        paymentIntent.setStatus(PaymentIntentStateMachine.transition(paymentIntent.getStatus(), PaymentIntentStatus.SUCCEEDED));
        paymentIntentRepository.save(paymentIntent);

        return PaymentIntentMapper.toResponseDTO(paymentIntent);
    }
}
