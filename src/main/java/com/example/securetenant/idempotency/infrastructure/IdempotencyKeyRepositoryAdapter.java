package com.example.securetenant.idempotency.infrastructure;

import com.example.securetenant.idempotency.application.IdempotencyKeyRepository;
import com.example.securetenant.idempotency.domain.IdempotencyKey;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class IdempotencyKeyRepositoryAdapter implements IdempotencyKeyRepository {

    private final SpringDataIdempotencyKeyRepository repository;

    public IdempotencyKeyRepositoryAdapter(SpringDataIdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<IdempotencyKey> findByTenantIdAndKey(String tenantId, String key) {
        return repository.findByTenantIdAndKey(tenantId, key).map(IdempotencyKeyJpaEntity::toDomain);
    }

    @Override
    public IdempotencyKey saveAndFlush(IdempotencyKey key) {
        return repository.saveAndFlush(IdempotencyKeyJpaEntity.fromDomain(key)).toDomain();
    }

    @Override
    public IdempotencyKey save(IdempotencyKey key) {
        IdempotencyKeyJpaEntity entity = repository.findById(key.id())
                .orElseGet(() -> IdempotencyKeyJpaEntity.fromDomain(key));
        entity.apply(key);
        return repository.save(entity).toDomain();
    }
}
