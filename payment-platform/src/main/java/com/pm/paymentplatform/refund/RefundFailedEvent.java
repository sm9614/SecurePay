package com.pm.paymentplatform.refund;

import java.util.UUID;

public record RefundFailedEvent(UUID eventId,
                                UUID refundId,
                                UUID paymentIntentId,
                                Long amountMinorUnits) {
}
