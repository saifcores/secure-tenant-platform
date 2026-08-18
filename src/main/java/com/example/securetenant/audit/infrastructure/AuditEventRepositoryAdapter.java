package com.example.securetenant.audit.infrastructure;

import com.example.securetenant.audit.application.AuditEventRepository;
import com.example.securetenant.audit.domain.AuditEvent;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AuditEventRepositoryAdapter implements AuditEventRepository {

    private final SpringDataAuditEventRepository repository;

    public AuditEventRepositoryAdapter(SpringDataAuditEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuditEvent save(AuditEvent event) {
        return repository.save(AuditEventJpaEntity.fromDomain(event)).toDomain();
    }

    @Override
    public List<AuditEvent> findByTenantId(String tenantId) {
        return repository.findByTenantIdOrderByOccurredAtDesc(tenantId).stream()
                .map(AuditEventJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<AuditEvent> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "occurredAt")).stream()
                .map(AuditEventJpaEntity::toDomain)
                .toList();
    }
}
