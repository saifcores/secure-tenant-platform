package com.example.securetenant.idempotency.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataIdempotencyKeyRepository extends JpaRepository<IdempotencyKeyJpaEntity, UUID> {

    Optional<IdempotencyKeyJpaEntity> findByTenantIdAndKey(String tenantId, String key);
}
