package com.example.securetenant.payment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataSettlementRepository extends JpaRepository<SettlementJpaEntity, UUID> {

    Optional<SettlementJpaEntity> findByPaymentId(UUID paymentId);
}
