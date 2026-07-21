package com.pm.paymentplatform.refund;

public class RefundExceedsAvailableBalanceException extends RuntimeException {

    private final Long remainingBalance;

    private final Long requestAmount;

    public RefundExceedsAvailableBalanceException(Long remainingBalance, Long requestAmount) {
        super("The requested amount: " + requestAmount + " is greater than the available balance: " + remainingBalance);

        this.remainingBalance = remainingBalance;
        this.requestAmount = requestAmount;
    }

    public Long getRemainingBalance() {
        return remainingBalance;
    }

    public Long getRequestAmount() {
        return requestAmount;
    }
}
