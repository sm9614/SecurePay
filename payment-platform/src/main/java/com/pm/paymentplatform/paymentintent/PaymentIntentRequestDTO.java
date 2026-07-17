package com.pm.paymentplatform.paymentintent;

import jakarta.validation.constraints.*;

import java.util.Currency;

public class PaymentIntentRequestDTO {

    @NotNull
    @Min(1)
    private Long amountMinorUnits;

    @NotNull
    private Currency currency;

    public Long getAmountMinorUnits() {
        return amountMinorUnits;
    }

    public void setAmountMinorUnits(Long amountMinorUnits) {
        this.amountMinorUnits = amountMinorUnits;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

}
