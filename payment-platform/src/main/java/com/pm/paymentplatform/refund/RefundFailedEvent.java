package com.pm.paymentplatform.refund;

import java.util.UUID;

public record RefundFailedEvent(UUID refundId, UUID paymentIntentId, Long amountMinorUnits) {
}
