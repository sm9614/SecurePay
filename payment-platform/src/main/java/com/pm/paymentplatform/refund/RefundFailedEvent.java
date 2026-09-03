package com.pm.paymentplatform.refund;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

public record RefundFailedEvent(UUID eventId,
                                UUID refundId,
                                UUID paymentIntentId,
                                UUID merchantId,
                                Long amountMinorUnits,
                                Currency currency,
                                Instant occurredAt) {
}
