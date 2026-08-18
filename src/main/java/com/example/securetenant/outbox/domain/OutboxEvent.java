package com.example.securetenant.outbox.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record OutboxEvent(
        UUID id,
        String tenantId,
        String aggregateType,
        String aggregateId,
        String eventType,
        Map<String, Object> payload,
        Instant createdAt,
        Instant publishedAt
) {
}
