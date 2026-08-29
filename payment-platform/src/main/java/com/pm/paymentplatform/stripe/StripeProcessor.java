package com.pm.paymentplatform.stripe;

import com.pm.paymentplatform.payment.PaymentProcessor;
import com.pm.paymentplatform.payment.ProcessorResult;
import com.stripe.StripeClient;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class StripeProcessor implements PaymentProcessor {

    private final StripeClient stripeClient;
    private final Logger log =  LoggerFactory.getLogger(StripeProcessor.class);
    private final static String TEST_PAYMENT_METHOD = "pm_card_visa";

    public StripeProcessor(StripeClient stripeClient) {
        this.stripeClient = stripeClient;
    }

    @Override
    public ProcessorResult processRefund(String paymentIntentReference,
                                         Long amountMinorUnits,
                                         String idempotencyKey) {
        RefundCreateParams params = RefundCreateParams
                .builder()
                .setPaymentIntent(paymentIntentReference)
                .setAmount(amountMinorUnits)
                .build();

        RequestOptions options = RequestOptions
                .builder()
                .setIdempotencyKey(idempotencyKey)
                .build();

        try {
            Refund refund = stripeClient.v1().refunds().create(params, options);
            return new ProcessorResult.Success(refund.getId());

        } catch (CardException e) {
            return new ProcessorResult.Declined(e.getDeclineCode(), e.getMessage());

        }catch (StripeException e) {
            return new ProcessorResult.ProcessorError(e.getMessage());
        }
    }

    @Override
    public ProcessorResult processPayment(Long amountMinorUnits,
                                          Currency currency,
                                          String idempotencyKey) {
        PaymentIntentCreateParams params = PaymentIntentCreateParams
                .builder()
                .setAmount(amountMinorUnits)
                .setCurrency(currency.getCurrencyCode().toLowerCase())
                .setPaymentMethod(TEST_PAYMENT_METHOD)
                .setConfirm(true)
                .addPaymentMethodType("card")
                .build();

        RequestOptions options = RequestOptions
                .builder()
                .setIdempotencyKey(idempotencyKey)
                .build();

        try {
            PaymentIntent paymentIntent = stripeClient.v1().paymentIntents().create(params, options);
            return new ProcessorResult.Success(paymentIntent.getId());
        } catch (CardException e) {
            log.warn("Stripe card declined: declineCode={}, message={}", e.getDeclineCode(), e.getMessage());
            return new ProcessorResult.Declined(e.getDeclineCode(), e.getMessage());

        }catch (StripeException e) {
            log.warn("Stripe processor error: message={}", e.getMessage());
            return new ProcessorResult.ProcessorError(e.getMessage());
        }
    }
}
