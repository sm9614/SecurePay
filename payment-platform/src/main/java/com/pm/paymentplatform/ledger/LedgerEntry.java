package com.pm.paymentplatform.ledger;

import com.pm.paymentplatform.paymentintent.PaymentIntent;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private PaymentIntent paymentIntent;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private EntryType entryType;

    @Column(name = "amount_minor_units", nullable = false)
    private Long amountMinorUnits;

    @Column(name = "currency", nullable = false)
    private Currency currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public PaymentIntent getPaymentIntent() {
        return paymentIntent;
    }

    public void setPaymentIntent(PaymentIntent paymentIntent) {
        this.paymentIntent = paymentIntent;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(EntryType entryType) {
        this.entryType = entryType;
    }

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

    public Instant getCreatedAt() {
        return createdAt;
    }

}
