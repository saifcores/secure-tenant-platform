package com.example.securetenant.idempotency.domain;

import java.time.Instant;
import java.util.UUID;

public record IdempotencyKey(
        UUID id,
        String tenantId,
        String key,
        String requestHash,
        String responseBody,
        Integer httpStatus,
        Instant createdAt
) {
}
