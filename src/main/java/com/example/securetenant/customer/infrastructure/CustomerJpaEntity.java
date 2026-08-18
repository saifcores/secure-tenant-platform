package com.example.securetenant.customer.infrastructure;

import com.example.securetenant.customer.domain.Customer;
import com.example.securetenant.customer.domain.CustomerStatus;
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
@Table(name = "customers")
public class CustomerJpaEntity extends AuditedEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false, length = 64)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CustomerStatus status;

    protected CustomerJpaEntity() {
    }

    public Customer toDomain() {
        return new Customer(id, tenantId, name, email, phone, status, getCreatedAt(), getUpdatedAt());
    }

    public static CustomerJpaEntity fromDomain(Customer customer) {
        CustomerJpaEntity entity = new CustomerJpaEntity();
        entity.id = customer.id();
        entity.tenantId = customer.tenantId();
        entity.apply(customer);
        return entity;
    }

    public void apply(Customer customer) {
        this.name = customer.name();
        this.email = customer.email();
        this.phone = customer.phone();
        this.status = customer.status();
    }

    public UUID getId() {
        return id;
    }
}
