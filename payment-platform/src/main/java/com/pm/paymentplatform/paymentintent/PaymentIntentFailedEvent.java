package com.pm.paymentplatform.paymentintent;

import com.pm.paymentplatform.stripe.FailureReason;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

public record PaymentIntentFailedEvent(UUID eventId,
                                       UUID paymentIntentID,
                                       UUID merchantId,
                                       Long amountMinorUnits,
                                       Currency currency,
                                       Instant occurredAt,
                                       FailureReason failureReason,
                                       String reasonDetail) {
}
