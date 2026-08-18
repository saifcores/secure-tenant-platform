package com.example.securetenant.customer.domain;

import java.time.Instant;
import java.util.UUID;

public record Customer(
                UUID id,
                String tenantId,
                String name,
                String email,
                String phone,
                CustomerStatus status,
                Instant createdAt,
                Instant updatedAt) {
}
