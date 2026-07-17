package com.pm.paymentplatform.paymentintent;

import com.pm.paymentplatform.idempotency.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, UUID> {

}
