package com.pm.paymentplatform.idempotency;

public class IdempotencyKeyMapper {
    public static IdempotencyKeyResponseDTO toResponseDTO(IdempotencyKey entity) {
        IdempotencyKeyResponseDTO idempotencyKeyResponseDTO = new IdempotencyKeyResponseDTO();
        idempotencyKeyResponseDTO.setId(entity.getId());
        idempotencyKeyResponseDTO.setIdempotencyKey(entity.getIdempotencyKey());
        idempotencyKeyResponseDTO.setOperationType(entity.getOperationType());
        idempotencyKeyResponseDTO.setStatus(entity.getStatus());
        idempotencyKeyResponseDTO.setResponseStatus(entity.getResponseStatus());
        idempotencyKeyResponseDTO.setResponseBody(entity.getResponseBody());
        idempotencyKeyResponseDTO.setCreatedAt(entity.getCreatedAt());
        return idempotencyKeyResponseDTO;
    }
}
