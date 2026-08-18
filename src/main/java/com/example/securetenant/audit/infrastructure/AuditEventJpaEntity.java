package com.example.securetenant.audit.infrastructure;

import com.example.securetenant.audit.domain.AuditEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEventJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", length = 64)
    private String tenantId;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(name = "resource_type", nullable = false, length = 64)
    private String resourceType;

    @Column(name = "resource_id", length = 128)
    private String resourceId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    protected AuditEventJpaEntity() {
    }

    public static AuditEventJpaEntity fromDomain(AuditEvent event) {
        AuditEventJpaEntity entity = new AuditEventJpaEntity();
        entity.id = event.id();
        entity.tenantId = event.tenantId();
        entity.userId = event.userId();
        entity.action = event.action();
        entity.resourceType = event.resourceType();
        entity.resourceId = event.resourceId();
        entity.occurredAt = event.timestamp();
        entity.ipAddress = event.ipAddress();
        entity.metadata = event.metadata();
        return entity;
    }

    public AuditEvent toDomain() {
        return new AuditEvent(
                id,
                tenantId,
                userId,
                action,
                resourceType,
                resourceId,
                occurredAt,
                ipAddress,
                metadata);
    }
}
