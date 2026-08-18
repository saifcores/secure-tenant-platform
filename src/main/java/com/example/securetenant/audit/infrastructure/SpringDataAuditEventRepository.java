package com.example.securetenant.audit.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataAuditEventRepository extends JpaRepository<AuditEventJpaEntity, UUID> {

    List<AuditEventJpaEntity> findByTenantIdOrderByOccurredAtDesc(String tenantId);
}
