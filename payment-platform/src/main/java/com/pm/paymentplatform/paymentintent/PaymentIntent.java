package com.pm.paymentplatform.paymentintent;

import com.pm.paymentplatform.idempotency.IdempotencyKey;
import com.pm.paymentplatform.merchant.Merchant;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Entity
@Table(name = "payment_intents")
public class PaymentIntent {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "processor_reference")
    private String processorReference;

    @Column(name = "amount_minor_units", nullable = false)
    // Stores money in smaller unit (cents) unless the currency doesn't have a smaller unit
    private Long amountMinorUnits;

    @Column(name = "currency", nullable = false)
    private Currency currency;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idempotency_key_id", nullable = false)
    private IdempotencyKey idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentIntentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProcessorReference() {
        return processorReference;
    }

    public void setProcessorReference(String processorReference) {
        this.processorReference = processorReference;
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

    public IdempotencyKey getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(IdempotencyKey idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public PaymentIntentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentIntentStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }
}
