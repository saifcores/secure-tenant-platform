package com.example.securetenant.payment.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Payment(
        UUID id,
        String tenantId,
        UUID orderId,
        UUID walletId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String failureReason,
        int attemptCount,
        Instant nextRetryAt,
        Instant createdAt,
        Instant updatedAt
) {
}
