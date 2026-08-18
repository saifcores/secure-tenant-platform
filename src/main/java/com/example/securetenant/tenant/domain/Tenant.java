package com.example.securetenant.tenant.domain;

import java.time.Instant;
import java.util.UUID;

public record Tenant(
        UUID id,
        String identifier,
        String name,
        TenantStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public boolean isEnabled() {
        return status == TenantStatus.ACTIVE;
    }
}
