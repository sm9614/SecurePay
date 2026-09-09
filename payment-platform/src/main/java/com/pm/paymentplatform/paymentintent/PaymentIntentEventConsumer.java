package com.pm.paymentplatform.paymentintent;

import com.pm.paymentplatform.messaging.EventType;
import com.pm.paymentplatform.messaging.ProcessedEvent;
import com.pm.paymentplatform.messaging.ProcessedEventRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class PaymentIntentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentIntentEventConsumer.class);
    private final ObjectMapper objectMapper;
    private final ProcessedEventRepository processedEventRepository;

    public PaymentIntentEventConsumer(ObjectMapper objectMapper,
                                      ProcessedEventRepository processedEventRepository) {
        this.objectMapper = objectMapper;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(topics = "payment-intent-events", groupId = "${kafka.consumer.payment-intent-group-id}")
    @Transactional
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            Header header = record.headers().lastHeader("event-type");
            if (header == null) {
                log.error("Missing event-type header, skipping message at offset {}", record.offset());
                acknowledgment.acknowledge();
                return;
            }

            EventType eventType = EventType.valueOf(new String(header.value(), StandardCharsets.UTF_8));
            String message = record.value();

            UUID eventId = switch (eventType) {
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
            };

            if (processedEventRepository.existsById(eventId)) {
                log.info("Payment intent already exists with id {}", eventId);
                acknowledgment.acknowledge();
                return;
            }
            ProcessedEvent processedEvent = new ProcessedEvent();
            processedEvent.setEventId(eventId);
            processedEventRepository.save(processedEvent);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
