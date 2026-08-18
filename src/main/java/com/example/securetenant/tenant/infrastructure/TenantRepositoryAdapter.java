package com.example.securetenant.tenant.infrastructure;

import com.example.securetenant.tenant.application.TenantRepository;
import com.example.securetenant.tenant.domain.Tenant;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TenantRepositoryAdapter implements TenantRepository {

    private final SpringDataTenantRepository repository;

    public TenantRepositoryAdapter(SpringDataTenantRepository repository) {
        this.repository = repository;
    }

    @Override
    public Tenant save(Tenant tenant) {
        TenantJpaEntity entity = repository.findById(tenant.id())
                .orElseGet(() -> TenantJpaEntity.fromDomain(tenant));
        entity.apply(tenant);
        return repository.save(entity).toDomain();
    }

    @Override
    public Optional<Tenant> findById(UUID id) {
        return repository.findById(id).map(TenantJpaEntity::toDomain);
    }

    @Override
    public Optional<Tenant> findByIdentifier(String identifier) {
        return repository.findByIdentifier(identifier).map(TenantJpaEntity::toDomain);
    }

    @Override
    public List<Tenant> findAll() {
        return repository.findAll().stream().map(TenantJpaEntity::toDomain).toList();
    }

    @Override
    public boolean existsByIdentifier(String identifier) {
        return repository.existsByIdentifier(identifier);
    }
}
