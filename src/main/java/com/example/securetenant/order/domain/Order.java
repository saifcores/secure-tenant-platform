package com.example.securetenant.order.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Order(
                UUID id,
                String tenantId,
                UUID customerId,
                BigDecimal amount,
                String currency,
                OrderStatus status,
                Instant createdAt,
                Instant updatedAt) {
}
