package com.example.securetenant.tenant.application;

import com.example.securetenant.tenant.domain.Tenant;
import com.example.securetenant.tenant.domain.TenantStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantRepository {

    Tenant save(Tenant tenant);

    Optional<Tenant> findById(UUID id);

    Optional<Tenant> findByIdentifier(String identifier);

    List<Tenant> findAll();

    boolean existsByIdentifier(String identifier);
}
