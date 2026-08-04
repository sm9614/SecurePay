package com.pm.paymentplatform.merchant;

public class MerchantMapper {

    public static MerchantRegistrationResponseDTO toResponseDTO(Merchant entity) {
        MerchantRegistrationResponseDTO response = new MerchantRegistrationResponseDTO();
        response.setId(entity.getId());
        response.setEmail(entity.getEmail());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdateAt(entity.getUpdatedAt());
        response.setRole(entity.getRole());
        return response;
    }
}
