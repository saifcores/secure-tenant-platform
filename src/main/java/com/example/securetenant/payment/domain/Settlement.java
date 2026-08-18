package com.example.securetenant.payment.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Settlement(
        UUID id,
        String tenantId,
        UUID paymentId,
        BigDecimal amount,
        String currency,
        SettlementStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
