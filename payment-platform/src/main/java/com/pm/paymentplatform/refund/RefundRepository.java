package com.pm.paymentplatform.refund;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface RefundRepository extends JpaRepository<Refund, UUID> {

    @Query("SELECT COALESCE(sum(r.amountMinorUnits), 0) FROM Refund r where r.paymentIntent.id = :id AND r.status NOT IN ('FAILED', 'CANCELED')")
    Long sumReservedAmountByPaymentIntentId(@Param("id") UUID id);
}
