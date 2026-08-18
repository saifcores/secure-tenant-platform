package com.example.securetenant.payment.infrastructure;

import com.example.securetenant.payment.application.SettlementRepository;
import com.example.securetenant.payment.domain.Settlement;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SettlementRepositoryAdapter implements SettlementRepository {

    private final SpringDataSettlementRepository repository;

    public SettlementRepositoryAdapter(SpringDataSettlementRepository repository) {
        this.repository = repository;
    }

    @Override
    public Settlement save(Settlement settlement) {
        return repository.save(SettlementJpaEntity.fromDomain(settlement)).toDomain();
    }

    @Override
    public Optional<Settlement> findByPaymentId(UUID paymentId) {
        return repository.findByPaymentId(paymentId).map(SettlementJpaEntity::toDomain);
    }

    @Override
    public List<Settlement> findAll() {
        return repository.findAll().stream().map(SettlementJpaEntity::toDomain).toList();
    }
}
