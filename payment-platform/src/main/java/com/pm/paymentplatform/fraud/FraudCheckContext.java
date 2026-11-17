package com.pm.paymentplatform.fraud;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

public record FraudCheckContext(
        // Core transaction fields (proto 1-5)
        UUID transactionId,
        UUID merchantId,
        long amountMinorUnits,
        Currency currency,
        Instant occurredAt,

        // Precomputed context (proto 6-12)
        int merchantTransactionCountToday,
        long merchantAverageAmountMinorUnits,
        int velocityCount5min,
        int velocityCount1hr,
        int priorDeclineCount24h,
        float priorRefundRate,
        boolean isNewMerchant
) {}
