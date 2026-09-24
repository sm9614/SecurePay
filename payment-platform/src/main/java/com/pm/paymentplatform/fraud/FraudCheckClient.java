package com.pm.paymentplatform.fraud;

public interface FraudCheckClient {
    FraudDecision checkTransaction(FraudCheckContext context);
}
