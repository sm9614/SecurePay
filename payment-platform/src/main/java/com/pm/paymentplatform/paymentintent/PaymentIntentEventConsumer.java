package com.pm.paymentplatform.paymentintent;

import com.pm.paymentplatform.messaging.EventRecordProcessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
public class PaymentIntentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentIntentEventConsumer.class);
    private final ObjectMapper objectMapper;
    private final EventRecordProcessor eventRecordProcessor;

    public PaymentIntentEventConsumer(ObjectMapper objectMapper,
                                      EventRecordProcessor eventRecordProcessor) {
        this.objectMapper = objectMapper;
        this.eventRecordProcessor = eventRecordProcessor;
    }

    @KafkaListener(topics = "payment-intent-events", groupId = "${spring.kafka.consumer.payment-intent-group-id}")
    @Transactional
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        eventRecordProcessor.processRecord(record, acknowledgment, (eventType, message) -> switch (eventType) {
            case PAYMENT_INTENT_SUCCEEDED -> {
                PaymentIntentSucceededEvent event = objectMapper.readValue(message, PaymentIntentSucceededEvent.class);
                log.info("Payment intent succeeded with event {}", event);
                yield event.eventId();
            }
            case PAYMENT_INTENT_FAILED -> {
                PaymentIntentFailedEvent event = objectMapper.readValue(message, PaymentIntentFailedEvent.class);
                log.info("Payment intent failed with event {}", event);
                yield event.eventId();
            }
            default -> throw new IllegalStateException("Unknown event type " + eventType);
        });
    }
}