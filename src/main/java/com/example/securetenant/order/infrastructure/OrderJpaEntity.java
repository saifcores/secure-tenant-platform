package com.example.securetenant.order.infrastructure;

import com.example.securetenant.order.domain.Order;
import com.example.securetenant.order.domain.OrderStatus;
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
@Table(name = "orders")
public class OrderJpaEntity extends AuditedEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false, length = 64)
    private String tenantId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    protected OrderJpaEntity() {
    }

    public Order toDomain() {
        return new Order(id, tenantId, customerId, amount, currency, status, getCreatedAt(), getUpdatedAt());
    }

    public static OrderJpaEntity fromDomain(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.id = order.id();
        entity.tenantId = order.tenantId();
        entity.apply(order);
        return entity;
    }

    public void apply(Order order) {
        this.customerId = order.customerId();
        this.amount = order.amount();
        this.currency = order.currency();
        this.status = order.status();
    }
}
