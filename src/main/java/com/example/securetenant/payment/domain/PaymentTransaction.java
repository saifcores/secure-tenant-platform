package com.example.securetenant.payment.domain;

import java.time.Instant;
import java.util.UUID;

public record PaymentTransaction(
        UUID id,
        String tenantId,
        UUID paymentId,
        int attempt,
        String pspReference,
        PaymentTransactionStatus status,
        String errorCode,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt
) {
}
