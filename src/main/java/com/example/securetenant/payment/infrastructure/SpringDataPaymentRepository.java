package com.example.securetenant.payment.infrastructure;

import com.example.securetenant.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SpringDataPaymentRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    List<PaymentJpaEntity> findByStatusInAndNextRetryAtLessThanEqualAndAttemptCountLessThan(
            Collection<PaymentStatus> statuses,
            Instant nextRetryAt,
            int attemptCount
    );

    boolean existsByOrderIdAndStatusIn(UUID orderId, Collection<PaymentStatus> statuses);
}
