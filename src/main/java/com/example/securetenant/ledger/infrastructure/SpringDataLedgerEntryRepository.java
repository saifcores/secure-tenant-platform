package com.example.securetenant.ledger.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataLedgerEntryRepository extends JpaRepository<LedgerEntryJpaEntity, UUID> {

    List<LedgerEntryJpaEntity> findByPaymentIdOrderByCreatedAtAsc(UUID paymentId);

    List<LedgerEntryJpaEntity> findByWalletIdOrderByCreatedAtAsc(UUID walletId);
}
