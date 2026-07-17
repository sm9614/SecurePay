package com.pm.paymentplatform.paymentintent;

public class PaymentIntentMapper {

    public static PaymentIntentResponseDTO toResponseDTO(PaymentIntent entity) {

        PaymentIntentResponseDTO response = new PaymentIntentResponseDTO();
        response.setId(entity.getId());
        response.setAmountMinorUnits(entity.getAmountMinorUnits());
        response.setCurrency(entity.getCurrency());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        return response;
    }
}
