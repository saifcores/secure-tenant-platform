package com.example.securetenant.ledger.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntry(
        UUID id,
        String tenantId,
        UUID paymentId,
        UUID walletId,
        LedgerAccount account,
        LedgerDirection direction,
        BigDecimal amount,
        String currency,
        Instant createdAt
) {
}
