package com.pm.paymentplatform.paymentintent;

import com.pm.paymentplatform.fraud.FraudCheckContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Component
public class FraudCheckContextAssembler {
    public FraudCheckContext assemble(UUID paymentIntentId,
                                      UUID merchantId,
                                      long amountMinorUnits,
                                      Currency currency) {
        
        //todo replace the default values
        return new FraudCheckContext(
                paymentIntentId,
                merchantId,
                amountMinorUnits,
                currency,
                Instant.now(),
                0,
                0,
                0,
                0,
                0,
                0f,
                false
                );
    }
}
