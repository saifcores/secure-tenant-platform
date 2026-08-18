package com.example.securetenant.payment.infrastructure;

import com.example.securetenant.payment.domain.Payment;
import com.example.securetenant.payment.domain.PaymentStatus;
import com.example.securetenant.shared.persistence.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class PaymentJpaEntity extends AuditedEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false, length = 64)
    private String tenantId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    protected PaymentJpaEntity() {
    }

    public Payment toDomain() {
        return new Payment(
                id,
                tenantId,
                orderId,
                walletId,
                amount,
                currency,
                status,
                failureReason,
                attemptCount,
                nextRetryAt,
                getCreatedAt(),
                getUpdatedAt()
        );
    }

    public static PaymentJpaEntity fromDomain(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.id = payment.id();
        entity.tenantId = payment.tenantId();
        entity.apply(payment);
        return entity;
    }

    public void apply(Payment payment) {
        this.orderId = payment.orderId();
        this.walletId = payment.walletId();
        this.amount = payment.amount();
        this.currency = payment.currency();
        this.status = payment.status();
        this.failureReason = payment.failureReason();
        this.attemptCount = payment.attemptCount();
        this.nextRetryAt = payment.nextRetryAt();
    }
}
