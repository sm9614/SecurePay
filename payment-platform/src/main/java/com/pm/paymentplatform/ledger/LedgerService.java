package com.pm.paymentplatform.ledger;

import com.pm.paymentplatform.paymentintent.PaymentIntent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public DoubleEntryResult recordDoubleEntry(PaymentIntent paymentIntent) {
        UUID transactionId = UUID.randomUUID();

        LedgerEntry debit = new LedgerEntry();
        LedgerEntry credit = new LedgerEntry();

        debit.setTransactionId(transactionId);
        credit.setTransactionId(transactionId);

        debit.setCurrency(paymentIntent.getCurrency());
        credit.setCurrency(paymentIntent.getCurrency());

        debit.setAmountMinorUnits(paymentIntent.getAmountMinorUnits());
        credit.setAmountMinorUnits(paymentIntent.getAmountMinorUnits());

        debit.setEntryType(EntryType.DEBIT);
        credit.setEntryType(EntryType.CREDIT);

        debit.setPaymentIntent(paymentIntent);
        credit.setPaymentIntent(paymentIntent);

        ledgerEntryRepository.save(debit);
        ledgerEntryRepository.save(credit);

        return new DoubleEntryResult(debit, credit);
    }
}
