package com.pm.paymentplatform.refund;

import com.pm.paymentplatform.messaging.EventRecordProcessor;
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
import java.time.Instant;
import java.util.UUID;

@Component
public class RefundEventConsumer {

    private final static Logger log = LoggerFactory.getLogger(RefundEventConsumer.class);
    private final ObjectMapper objectMapper;
    private final EventRecordProcessor eventRecordProcessor;

    public RefundEventConsumer(ObjectMapper objectMapper,
                               ProcessedEventRepository processedEventRepository, EventRecordProcessor eventRecordProcessor) {
        this.objectMapper = objectMapper;
        this.eventRecordProcessor = eventRecordProcessor;
    }

    @KafkaListener(topics = "refund-events", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        eventRecordProcessor.processRecord(record, acknowledgment, (eventType, message) -> switch (eventType) {
            case REFUND_SUCCEEDED -> {
                RefundSucceededEvent event = objectMapper.readValue(message, RefundSucceededEvent.class);
                log.info("Received refund succeeded event: {}", event);
                yield event.eventId();
            }
            case REFUND_FAILED -> {
                RefundFailedEvent event = objectMapper.readValue(message, RefundFailedEvent.class);
                log.info("Received refund failed event: {}", event);
                yield event.eventId();
            }
            default -> throw new IllegalStateException("Unexpected event type on refund-events topic: " + eventType);
        });
    }
}
