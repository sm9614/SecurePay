package com.pm.paymentplatform.paymentintent;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

public record PaymentIntentSucceededEvent(UUID eventId,
                                          UUID paymentIntentID,
                                          UUID merchantId,
                                          Long amountMinorUnits,
                                          Currency currency,
                                          Instant occurredAt,
                                          String processorReference) {
}
