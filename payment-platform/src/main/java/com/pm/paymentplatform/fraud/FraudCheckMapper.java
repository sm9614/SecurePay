package com.pm.paymentplatform.fraud;

import com.google.protobuf.Timestamp;
import com.pm.fraud.grpc.FraudCheckRequest;
import com.pm.fraud.grpc.FraudCheckResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FraudCheckMapper {
    private static final Logger log = LoggerFactory.getLogger(FraudCheckMapper.class);

    public FraudCheckRequest toRequest(FraudCheckContext context) {
        Timestamp occurredAt = Timestamp.newBuilder()
                .setSeconds(context.occurredAt().getEpochSecond())
                .setNanos(context.occurredAt().getNano())
                .build();

        return FraudCheckRequest.newBuilder()
                .setTransactionId(String.valueOf(context.transactionId()))
                .setMerchantId(String.valueOf(context.merchantId()))
                .setAmountMinorUnits(context.amountMinorUnits())
                .setCurrency(String.valueOf(context.currency()))
                .setOccurredAt(occurredAt)
                .setBillingCountry(context.billingCountry())
                .setIpCountry(context.ipCountry())
                .setIsFirstTimeCard(context.isFirstTimeCard())
                .setMerchantTransactionCountToday(context.merchantTransactionCountToday())
                .setMerchantAverageAmountMinorUnits(context.merchantAverageAmountMinorUnits())
                .setVelocityCount5Min(context.velocityCount5min())
                .setVelocityCount1Hr(context.velocityCount1hr())
                .setPriorDeclineCount24H(context.priorDeclineCount24h())
                .setPriorRefundRate(context.priorRefundRate())
                .setIsNewMerchant(context.isNewMerchant())
                .build();
    }

    public FraudDecision toFraudDecision(FraudCheckResponse response) {
        Decision decision = switch (response.getDecision()) {
            case ALLOW -> Decision.ALLOW;
            case REVIEW -> Decision.REVIEW;
            case BLOCK -> Decision.BLOCK;
            case UNRECOGNIZED, DECISION_UNSPECIFIED -> {
                log.warn("Fraud service returned unspecified decision, failing open");
                yield Decision.ALLOW;
            }
        };

        return new FraudDecision(
                decision,
                response.getRiskScore(),
                response.getReasonCodeList()
        );
    }

}
