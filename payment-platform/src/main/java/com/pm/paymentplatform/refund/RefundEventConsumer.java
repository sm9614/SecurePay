package com.pm.paymentplatform.refund;

import com.pm.paymentplatform.messaging.ProcessedEvent;
import com.pm.paymentplatform.messaging.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

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
    public void consume(String message, Acknowledgment acknowledgment) {
        try {
            RefundFailedEvent event = objectMapper.readValue(message, RefundFailedEvent.class);

            if (processedEventRepository.existsById(event.eventId())) {
                log.info("Refund event already exists: {}", event.eventId());
                acknowledgment.acknowledge();
                return;
            }
            log.info("Received refund event: {}", event);

            ProcessedEvent processedEvent = new ProcessedEvent();
            processedEvent.setEventId(event.eventId());
            processedEventRepository.save(processedEvent);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
