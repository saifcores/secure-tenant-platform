package com.example.securetenant.ledger.infrastructure;

import com.example.securetenant.ledger.application.LedgerEntryRepository;
import com.example.securetenant.ledger.domain.LedgerEntry;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class LedgerEntryRepositoryAdapter implements LedgerEntryRepository {

    private final SpringDataLedgerEntryRepository repository;

    public LedgerEntryRepositoryAdapter(SpringDataLedgerEntryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveAll(List<LedgerEntry> entries) {
        repository.saveAll(entries.stream().map(LedgerEntryJpaEntity::fromDomain).toList());
    }

    @Override
    public List<LedgerEntry> findByPaymentId(UUID paymentId) {
        return repository.findByPaymentIdOrderByCreatedAtAsc(paymentId).stream()
                .map(LedgerEntryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<LedgerEntry> findByWalletId(UUID walletId) {
        return repository.findByWalletIdOrderByCreatedAtAsc(walletId).stream()
                .map(LedgerEntryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<LedgerEntry> findAll() {
        return repository.findAll().stream().map(LedgerEntryJpaEntity::toDomain).toList();
    }
}
