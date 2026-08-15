package com.pm.paymentplatform.messaging;

import com.pm.paymentplatform.outbox.AggregateType;

public class KafkaTopicMapper {

    public static String map(AggregateType aggregateType) {
        return switch (aggregateType) {
            case REFUND ->  "refund_events";
            case PAYMENT_INTENT ->   "payment_intent-events";
        };
    }
}
