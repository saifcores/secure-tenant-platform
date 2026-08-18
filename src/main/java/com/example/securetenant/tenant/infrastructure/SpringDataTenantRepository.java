package com.example.securetenant.tenant.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataTenantRepository extends JpaRepository<TenantJpaEntity, UUID> {

    Optional<TenantJpaEntity> findByIdentifier(String identifier);

    boolean existsByIdentifier(String identifier);
}
