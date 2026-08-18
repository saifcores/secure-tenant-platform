package com.example.securetenant.outbox.infrastructure;

import com.example.securetenant.outbox.domain.OutboxEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEventJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", length = 64)
    private String tenantId;

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 128)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected OutboxEventJpaEntity() {
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public OutboxEvent toDomain() {
        return new OutboxEvent(
                id,
                tenantId,
                aggregateType,
                aggregateId,
                eventType,
                payload,
                createdAt,
                publishedAt
        );
    }

    public static OutboxEventJpaEntity fromDomain(OutboxEvent event) {
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.id = event.id();
        entity.tenantId = event.tenantId();
        entity.aggregateType = event.aggregateType();
        entity.aggregateId = event.aggregateId();
        entity.eventType = event.eventType();
        entity.payload = event.payload();
        entity.createdAt = event.createdAt();
        entity.publishedAt = event.publishedAt();
        return entity;
    }

    public void markPublished(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
}
