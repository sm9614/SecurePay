package com.pm.paymentplatform.paymentintent;

import com.pm.paymentplatform.idempotency.IdempotencyKey;
import org.springframework.stereotype.Service;

import java.util.Currency;

@Service
public class PaymentIntentService {

    private final PaymentIntentRepository paymentIntentRepository;

    public PaymentIntentService(PaymentIntentRepository paymentIntentRepository) {
        this.paymentIntentRepository = paymentIntentRepository;
    }

    public PaymentIntentResponseDTO createPaymentIntent(Long amountMinorUnits, Currency currency, IdempotencyKey idempotencyKey) {
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setAmountMinorUnits(amountMinorUnits);
        paymentIntent.setCurrency(currency);
        paymentIntent.setIdempotencyKey(idempotencyKey);
        paymentIntent.setStatus(PaymentIntentStatus.CREATED);

        paymentIntentRepository.save(paymentIntent);
        return PaymentIntentMapper.toResponseDTO(paymentIntent);
    }
}
