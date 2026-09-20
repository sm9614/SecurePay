package com.pm.paymentplatform.webhook;

import com.pm.paymentplatform.messaging.EventType;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class WebhookRelay {

    private final WebhookEndpointRepository webhookEndpointRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final ObjectMapper objectMapper;

    public WebhookRelay(WebhookEndpointRepository webhookEndpointRepository,
                        WebhookDeliveryRepository webhookDeliveryRepository,
                        ObjectMapper objectMapper) {
        this.webhookEndpointRepository = webhookEndpointRepository;
        this.webhookDeliveryRepository = webhookDeliveryRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = {"refund-events", "payment-intent-events"},
            groupId = "webhook-relay"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        UUID eventId = UUID.fromString(record.key());
        JsonNode node = objectMapper.readTree(record.value());
        EventType eventType = EventType.valueOf(node.get("eventType").asString());

        List<WebhookEndpoint> endpoints = webhookEndpointRepository.findActiveByEventType(eventType.name());

        if (endpoints.isEmpty()) {
            acknowledgment.acknowledge();
            return;
        }

        ObjectNode envelope = objectMapper.createObjectNode();
        envelope.put("eventId", eventId.toString());
        envelope.put("eventType", eventType.name());
        envelope.put("occurredAt", Instant.now().toString());
        envelope.set("data", node);
        String payload = objectMapper.writeValueAsString(envelope);

        for (WebhookEndpoint endpoint : endpoints) {
            WebhookDelivery webhookDelivery = new WebhookDelivery();
            webhookDelivery.setWebhookEndpoint(endpoint);
            webhookDelivery.setEventId(eventId);
            webhookDelivery.setEventType(eventType);
            webhookDelivery.setPayload(payload);
            webhookDelivery.setStatus(WebhookDeliveryStatus.PENDING);
            webhookDelivery.setAttemptCount(0);
            webhookDelivery.setNextAttemptAt(Instant.now());

            try {
                webhookDeliveryRepository.save(webhookDelivery);
            } catch (DataIntegrityViolationException ignored) {}
        }
        acknowledgment.acknowledge();
    }
}
