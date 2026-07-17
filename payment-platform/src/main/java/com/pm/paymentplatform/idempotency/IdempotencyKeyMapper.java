package com.pm.paymentplatform.idempotency;

public class IdempotencyKeyMapper {
    public static IdempotencyKeyResponseDTO toResponseDTO(IdempotencyKey entity) {

        IdempotencyKeyResponseDTO response = new IdempotencyKeyResponseDTO();
        response.setId(entity.getId());
        response.setIdempotencyKey(entity.getIdempotencyKey());
        response.setOperationType(entity.getOperationType());
        response.setStatus(entity.getStatus());
        response.setResponseStatus(entity.getResponseStatus());
        response.setResponseBody(entity.getResponseBody());
        response.setCreatedAt(entity.getCreatedAt());

        return response;
    }
}
