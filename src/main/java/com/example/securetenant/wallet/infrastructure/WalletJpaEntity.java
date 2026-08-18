package com.example.securetenant.wallet.infrastructure;

import com.example.securetenant.shared.persistence.AuditedEntity;
import com.example.securetenant.wallet.domain.Wallet;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class WalletJpaEntity extends AuditedEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "available_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal availableBalance;

    @Column(name = "reserved_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal reservedBalance;

    protected WalletJpaEntity() {
    }

    public Wallet toDomain() {
        return new Wallet(id, tenantId, currency, availableBalance, reservedBalance, getCreatedAt(), getUpdatedAt());
    }

    public static WalletJpaEntity fromDomain(Wallet wallet) {
        WalletJpaEntity entity = new WalletJpaEntity();
        entity.id = wallet.id();
        entity.tenantId = wallet.tenantId();
        entity.apply(wallet);
        return entity;
    }

    public void apply(Wallet wallet) {
        this.currency = wallet.currency();
        this.availableBalance = wallet.availableBalance();
        this.reservedBalance = wallet.reservedBalance();
    }
}
