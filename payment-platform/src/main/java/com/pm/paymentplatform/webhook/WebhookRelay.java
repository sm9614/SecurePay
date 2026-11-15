package com.pm.paymentplatform.webhook;

import com.pm.paymentplatform.messaging.EventType;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class WebhookRelay {

    private static final Logger log = LoggerFactory.getLogger(WebhookRelay.class);

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
    @KafkaListener(
            topics = {"refund-events", "payment-intent-events"},
            groupId = "webhook-relay"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {

        Header typeHeader = record.headers().lastHeader("event-type");
        if (typeHeader == null) {
            log.error("Missing event-type header, skipping record topic={} partition={} offset={}",
                    record.topic(), record.partition(), record.offset());
            acknowledgment.acknowledge();
            return;
        }
        EventType eventType = EventType.valueOf(new String(typeHeader.value(), StandardCharsets.UTF_8));

        JsonNode node = objectMapper.readTree(record.value());

        // CHANGED: guard the body fields we depend on
        if (!node.hasNonNull("eventId") || !node.hasNonNull("merchantId") || !node.hasNonNull("occurredAt")) {
            log.error("Malformed event body, skipping record topic={} partition={} offset={}",
                    record.topic(), record.partition(), record.offset());
            acknowledgment.acknowledge();
            return;
        }

        UUID eventId = UUID.fromString(node.get("eventId").asString());
        UUID merchantId = UUID.fromString(node.get("merchantId").asString());

        List<WebhookEndpoint> endpoints =
                webhookEndpointRepository.findActiveByMerchantIdAndEventType(merchantId, eventType.name());

        if (endpoints.isEmpty()) {
            acknowledgment.acknowledge();
            return;
        }

        ObjectNode envelope = objectMapper.createObjectNode();
        envelope.put("eventId", eventId.toString());
        envelope.put("eventType", eventType.name());
        envelope.put("occurredAt", node.get("occurredAt").asString());
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
