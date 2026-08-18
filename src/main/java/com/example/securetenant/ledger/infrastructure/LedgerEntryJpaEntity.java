package com.example.securetenant.ledger.infrastructure;

import com.example.securetenant.ledger.domain.LedgerAccount;
import com.example.securetenant.ledger.domain.LedgerDirection;
import com.example.securetenant.ledger.domain.LedgerEntry;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntryJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false, length = 64)
    private String tenantId;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "wallet_id")
    private UUID walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private LedgerAccount account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private LedgerDirection direction;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LedgerEntryJpaEntity() {
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public LedgerEntry toDomain() {
        return new LedgerEntry(id, tenantId, paymentId, walletId, account, direction, amount, currency, createdAt);
    }

    public static LedgerEntryJpaEntity fromDomain(LedgerEntry entry) {
        LedgerEntryJpaEntity entity = new LedgerEntryJpaEntity();
        entity.id = entry.id();
        entity.tenantId = entry.tenantId();
        entity.paymentId = entry.paymentId();
        entity.walletId = entry.walletId();
        entity.account = entry.account();
        entity.direction = entry.direction();
        entity.amount = entry.amount();
        entity.currency = entry.currency();
        entity.createdAt = entry.createdAt();
        return entity;
    }
}
