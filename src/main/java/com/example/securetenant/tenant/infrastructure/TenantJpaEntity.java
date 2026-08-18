package com.example.securetenant.tenant.infrastructure;

import com.example.securetenant.shared.persistence.AuditedEntity;
import com.example.securetenant.tenant.domain.Tenant;
import com.example.securetenant.tenant.domain.TenantStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "tenants")
public class TenantJpaEntity extends AuditedEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String identifier;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TenantStatus status;

    protected TenantJpaEntity() {
    }

    public TenantJpaEntity(UUID id, String identifier, String name, TenantStatus status) {
        this.id = id;
        this.identifier = identifier;
        this.name = name;
        this.status = status;
    }

    public Tenant toDomain() {
        return new Tenant(id, identifier, name, status, getCreatedAt(), getUpdatedAt());
    }

    public static TenantJpaEntity fromDomain(Tenant tenant) {
        return new TenantJpaEntity(tenant.id(), tenant.identifier(), tenant.name(), tenant.status());
    }

    public void apply(Tenant tenant) {
        this.name = tenant.name();
        this.status = tenant.status();
    }

    public UUID getId() {
        return id;
    }
}
