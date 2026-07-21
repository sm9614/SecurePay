package com.pm.paymentplatform.paymentintent;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, UUID> {
    Optional<PaymentIntent> getPaymentIntentById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p from PaymentIntent p where p.id = :id")
    Optional<PaymentIntent> getPaymentIntentByIdWithLock(@Param("id") UUID id);
}
