package com.pm.paymentplatform.refund;

import java.util.UUID;

public record RefundProcessingContext(UUID refundId,
                                      String processorReference,
                                      Long amountMinorUnits) {
}
