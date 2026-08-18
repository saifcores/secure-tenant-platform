package com.example.securetenant.audit.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditEvent(
                UUID id,
                String tenantId,
                String userId,
                String action,
                String resourceType,
                String resourceId,
                Instant timestamp,
                String ipAddress,
                Map<String, Object> metadata) {
}
