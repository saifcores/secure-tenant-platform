package com.example.securetenant.payment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataPaymentTransactionRepository extends JpaRepository<PaymentTransactionJpaEntity, UUID> {

    List<PaymentTransactionJpaEntity> findByPaymentIdOrderByAttemptAsc(UUID paymentId);
}
