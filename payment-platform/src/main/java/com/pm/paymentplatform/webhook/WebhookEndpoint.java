package com.pm.paymentplatform.webhook;

import com.pm.paymentplatform.merchant.Merchant;
import com.pm.paymentplatform.messaging.EventType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_endpoints")
public class WebhookEndpoint {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id" ,nullable = false)
    private Merchant merchant;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "signing_secret", nullable = false)
    private String signingSecret;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "subscribed_event_types", nullable = false)
    private EventType[] subscribedEventTypes;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSigningSecret() {
        return signingSecret;
    }

    public void setSigningSecret(String signingSecret) {
        this.signingSecret = signingSecret;
    }

    public EventType[] getSubscribedEventTypes() {
        return subscribedEventTypes;
    }

    public void setSubscribedEventTypes(EventType[] subscribedEventTypes) {
        this.subscribedEventTypes = subscribedEventTypes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
