package com.example.securetenant.payment.infrastructure;

import com.example.securetenant.payment.domain.PaymentTransaction;
import com.example.securetenant.payment.domain.PaymentTransactionStatus;
import com.example.securetenant.shared.persistence.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransactionJpaEntity extends AuditedEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false, length = 64)
    private String tenantId;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(nullable = false)
    private int attempt;

    @Column(name = "psp_reference", length = 128)
    private String pspReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentTransactionStatus status;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "error_message", length = 512)
    private String errorMessage;

    protected PaymentTransactionJpaEntity() {
    }

    public PaymentTransaction toDomain() {
        return new PaymentTransaction(
                id,
                tenantId,
                paymentId,
                attempt,
                pspReference,
                status,
                errorCode,
                errorMessage,
                getCreatedAt(),
                getUpdatedAt()
        );
    }

    public static PaymentTransactionJpaEntity fromDomain(PaymentTransaction tx) {
        PaymentTransactionJpaEntity entity = new PaymentTransactionJpaEntity();
        entity.id = tx.id();
        entity.tenantId = tx.tenantId();
        entity.paymentId = tx.paymentId();
        entity.attempt = tx.attempt();
        entity.pspReference = tx.pspReference();
        entity.status = tx.status();
        entity.errorCode = tx.errorCode();
        entity.errorMessage = tx.errorMessage();
        return entity;
    }
}
