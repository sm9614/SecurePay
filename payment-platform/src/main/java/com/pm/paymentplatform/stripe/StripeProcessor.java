package com.pm.paymentplatform.stripe;

import com.pm.paymentplatform.payment.PaymentProcessor;
import com.pm.paymentplatform.payment.ProcessorResult;
import com.stripe.StripeClient;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.param.RefundCreateParams;
import org.springframework.stereotype.Component;

@Component
public class StripeProcessor implements PaymentProcessor {

    private final StripeClient stripeClient;

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
}
