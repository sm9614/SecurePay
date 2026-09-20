package com.pm.paymentplatform.webhook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, UUID> {
    List<WebhookEndpoint> findByMerchantIdAndIsActiveTrue(UUID merchantId);

    @Query(value = "SELECT * FROM webhook_endpoints " +
            "WHERE is_active = true AND :eventType = ANY(subscribed_event_types)", nativeQuery = true)
    List<WebhookEndpoint> findActiveByEventType(@Param("eventType") String eventType);
}
