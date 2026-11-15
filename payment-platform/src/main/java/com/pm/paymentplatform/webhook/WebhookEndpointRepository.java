package com.pm.paymentplatform.webhook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, UUID> {

    @Query(value = "SELECT * FROM webhook_endpoints " +
            "WHERE merchant_id = :merchantId " +
            "AND is_active = true " +
            "AND :eventType = ANY(subscribed_event_types)", nativeQuery = true)
    List<WebhookEndpoint> findActiveByMerchantIdAndEventType(@Param("merchantId") UUID merchantId,
                                                             @Param("eventType") String eventType);
}
