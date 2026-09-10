package com.pm.paymentplatform.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Component
public class EventRecordProcessor {

    private static final Logger log = LoggerFactory.getLogger(EventRecordProcessor.class);
    private final ProcessedEventRepository processedEventRepository;

    public EventRecordProcessor(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    public void processRecord(ConsumerRecord<String, String> record,
                              Acknowledgment acknowledgment,
                              EventDispatcher dispatcher) {
        try {
            Header header = record.headers().lastHeader("event-type");
            if (header == null) {
                log.error("Missing event-type header, skipping message at offset {}", record.offset());
                acknowledgment.acknowledge();
                return;
            }

            EventType eventType = EventType.valueOf(new String(header.value(), StandardCharsets.UTF_8));
            String message = record.value();
            UUID eventId = dispatcher.dispatch(eventType, message);

            if (processedEventRepository.existsById(eventId)) {
                log.info("Event already processed: {}", eventId);
                acknowledgment.acknowledge();
                return;
            }

            ProcessedEvent processedEvent = new ProcessedEvent();
            processedEvent.setEventId(eventId);
            processedEvent.setProcessedAt(Instant.now());
            processedEventRepository.save(processedEvent);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
