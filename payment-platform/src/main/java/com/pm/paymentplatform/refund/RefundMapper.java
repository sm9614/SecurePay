package com.pm.paymentplatform.refund;

public class RefundMapper {

    public static RefundResponseDTO toResponseDTO(Refund entity) {
        RefundResponseDTO response = new RefundResponseDTO();

        response.setId(entity.getId());
        response.setStatus(entity.getStatus());
        response.setAmountMinorUnits(entity.getAmountMinorUnits());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        return response;
    }
}
