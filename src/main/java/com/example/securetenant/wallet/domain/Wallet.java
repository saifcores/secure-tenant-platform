package com.example.securetenant.wallet.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Wallet(
        UUID id,
        String tenantId,
        String currency,
        BigDecimal availableBalance,
        BigDecimal reservedBalance,
        Instant createdAt,
        Instant updatedAt
) {

    public BigDecimal totalBalance() {
        return availableBalance.add(reservedBalance);
    }
}
