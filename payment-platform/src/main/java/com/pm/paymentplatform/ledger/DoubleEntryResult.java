package com.pm.paymentplatform.ledger;

public record DoubleEntryResult(LedgerEntry debit, LedgerEntry credit) {
}
