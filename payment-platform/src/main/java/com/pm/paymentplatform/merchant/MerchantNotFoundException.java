package com.pm.paymentplatform.merchant;

import java.util.UUID;

public class MerchantNotFoundException extends RuntimeException {

    private final UUID id;

    public MerchantNotFoundException(UUID id) {
        super("Merchant with ID: " + id + " not found");
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
