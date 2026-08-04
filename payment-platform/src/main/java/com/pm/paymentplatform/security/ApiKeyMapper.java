package com.pm.paymentplatform.security;

public class ApiKeyMapper {
    public static ApiKeyResponseDTO toResponseDTO(ApiKey entity, String apiKey) {
        ApiKeyResponseDTO response = new ApiKeyResponseDTO();
        response.setApiKey(apiKey);
        response.setId(entity.getId());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}
