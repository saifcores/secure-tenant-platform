package com.example.securetenant.idempotency.application;

import com.example.securetenant.idempotency.domain.IdempotencyKey;

import java.util.Optional;

public interface IdempotencyKeyRepository {

    Optional<IdempotencyKey> findByTenantIdAndKey(String tenantId, String key);

    IdempotencyKey saveAndFlush(IdempotencyKey key);

    IdempotencyKey save(IdempotencyKey key);
}
