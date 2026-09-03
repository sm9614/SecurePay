package com.pm.paymentplatform.outbox;

import com.pm.paymentplatform.messaging.EventType;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventService(OutboxEventRepository outboxEventRepository,
                              ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void recordEvent(UUID id,
                            AggregateType aggregateType,
                            UUID aggregateId,
                            EventType eventType,
                            Object payload) {

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setId(id);
        outboxEvent.setStatus(OutboxStatus.PENDING);
        outboxEvent.setAggregateType(aggregateType);
        outboxEvent.setAggregateId(aggregateId);
        outboxEvent.setEventType(String.valueOf(eventType));
        outboxEvent.setPayload(objectMapper.writeValueAsString(payload));
        outboxEventRepository.save(outboxEvent);
    }
}
