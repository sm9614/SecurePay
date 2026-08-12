package com.pm.paymentplatform.outbox;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OutboxEvent o WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<OutboxEvent> findByStatusForUpdate(@Param("status") OutboxStatus status, Pageable pageable);
}
