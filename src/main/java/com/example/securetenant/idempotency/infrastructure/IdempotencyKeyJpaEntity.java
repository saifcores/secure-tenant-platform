package com.example.securetenant.idempotency.infrastructure;

import com.example.securetenant.idempotency.domain.IdempotencyKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKeyJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "key", nullable = false, length = 128)
    private String key;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "response_body")
    private String responseBody;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected IdempotencyKeyJpaEntity() {
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public IdempotencyKey toDomain() {
        return new IdempotencyKey(id, tenantId, key, requestHash, responseBody, httpStatus, createdAt);
    }

    public static IdempotencyKeyJpaEntity fromDomain(IdempotencyKey domain) {
        IdempotencyKeyJpaEntity entity = new IdempotencyKeyJpaEntity();
        entity.apply(domain);
        return entity;
    }

    public void apply(IdempotencyKey domain) {
        this.id = domain.id();
        this.tenantId = domain.tenantId();
        this.key = domain.key();
        this.requestHash = domain.requestHash();
        this.responseBody = domain.responseBody();
        this.httpStatus = domain.httpStatus();
        if (domain.createdAt() != null) {
            this.createdAt = domain.createdAt();
        }
    }
}
