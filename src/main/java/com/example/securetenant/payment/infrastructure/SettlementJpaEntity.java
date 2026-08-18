package com.example.securetenant.payment.infrastructure;

import com.example.securetenant.payment.domain.Settlement;
import com.example.securetenant.payment.domain.SettlementStatus;
import com.example.securetenant.shared.persistence.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "settlements")
public class SettlementJpaEntity extends AuditedEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false, length = 64)
    private String tenantId;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SettlementStatus status;

    protected SettlementJpaEntity() {
    }

    public Settlement toDomain() {
        return new Settlement(id, tenantId, paymentId, amount, currency, status, getCreatedAt(), getUpdatedAt());
    }

    public static SettlementJpaEntity fromDomain(Settlement settlement) {
        SettlementJpaEntity entity = new SettlementJpaEntity();
        entity.id = settlement.id();
        entity.tenantId = settlement.tenantId();
        entity.paymentId = settlement.paymentId();
        entity.amount = settlement.amount();
        entity.currency = settlement.currency();
        entity.status = settlement.status();
        return entity;
    }
}
