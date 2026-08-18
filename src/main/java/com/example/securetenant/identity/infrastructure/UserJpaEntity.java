package com.example.securetenant.identity.infrastructure;

import com.example.securetenant.identity.domain.User;
import com.example.securetenant.identity.domain.UserRole;
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
@Table(name = "users")
public class UserJpaEntity extends AuditedEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 128)
    private String username;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private UserRole role;

    protected UserJpaEntity() {
    }

    public User toDomain() {
        return new User(id, tenantId, username, email, role, getCreatedAt(), getUpdatedAt());
    }
}
