package com.pm.paymentplatform.fraud;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

public record FraudCheckContext(
        // Core transaction fields (proto 1-8)
        String transactionId,
        String merchantId,
        long amountMinorUnits,
        Currency currency,
        Instant occurredAt,
        String billingCountry,
        String ipCountry,
        boolean isFirstTimeCard,

        // Precomputed context (proto 9-15)
        int merchantTransactionCountToday,
        long merchantAverageAmountMinorUnits,
        int velocityCount5min,
        int velocityCount1hr,
        int priorDeclineCount24h,
        float priorRefundRate,
        boolean isNewMerchant
) {}
