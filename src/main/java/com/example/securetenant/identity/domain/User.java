package com.example.securetenant.identity.domain;

import java.time.Instant;
import java.util.UUID;

public record User(
        UUID id,
        String tenantId,
        String username,
        String email,
        UserRole role,
        Instant createdAt,
        Instant updatedAt
) {
}
