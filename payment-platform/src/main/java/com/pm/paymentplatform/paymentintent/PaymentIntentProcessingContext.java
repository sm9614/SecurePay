package com.pm.paymentplatform.paymentintent;

import java.util.Currency;
import java.util.UUID;

public record PaymentIntentProcessingContext(UUID paymentIntentId,
                                             Long amountMinorUnits,
                                             Currency currency) {
}
