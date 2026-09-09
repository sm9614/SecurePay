package com.pm.paymentplatform.refund;

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
public class RefundEventConsumer {

    private final static Logger log = LoggerFactory.getLogger(RefundEventConsumer.class);
    private final ObjectMapper objectMapper;
    private final ProcessedEventRepository processedEventRepository;

    public RefundEventConsumer(ObjectMapper objectMapper,
                               ProcessedEventRepository processedEventRepository) {
        this.objectMapper = objectMapper;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(topics = "refund-events", groupId = "${spring.kafka.consumer.group-id}")
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
            };

            if (processedEventRepository.existsById(eventId)) {
                log.info("Refund event already processed: {}", eventId);
                acknowledgment.acknowledge();
                return;
            }

            ProcessedEvent processedEvent = new ProcessedEvent();
            processedEvent.setEventId(eventId);
            processedEventRepository.save(processedEvent);
            acknowledgment.acknowledge();
        }catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
