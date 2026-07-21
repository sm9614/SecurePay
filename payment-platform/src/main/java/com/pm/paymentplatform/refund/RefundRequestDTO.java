package com.pm.paymentplatform.refund;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RefundRequestDTO {

    @NotNull
    @Min(1)
    Long amountMinorUnits;

    public Long getAmountMinorUnits() {
        return amountMinorUnits;
    }

    public void setAmountMinorUnits(Long amountMinorUnits) {
        this.amountMinorUnits = amountMinorUnits;
    }

}
