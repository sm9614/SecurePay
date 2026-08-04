package com.pm.paymentplatform.security;

import java.time.Instant;
import java.util.UUID;

public class ApiKeyResponseDTO {

    private UUID id;

    private String apiKey;

    private Instant createdAt;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
