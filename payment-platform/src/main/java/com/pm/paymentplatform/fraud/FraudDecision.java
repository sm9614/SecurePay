package com.pm.paymentplatform.fraud;

import java.util.List;

public record FraudDecision(Decision decision,
                            float riskScore,
                            List<String> reasonCodes) {
}
