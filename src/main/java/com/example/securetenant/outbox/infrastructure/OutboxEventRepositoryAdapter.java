package com.example.securetenant.outbox.infrastructure;

import com.example.securetenant.outbox.application.OutboxEventRepository;
import com.example.securetenant.outbox.domain.OutboxEvent;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class OutboxEventRepositoryAdapter implements OutboxEventRepository {

    private final SpringDataOutboxEventRepository repository;

    public OutboxEventRepositoryAdapter(SpringDataOutboxEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public OutboxEvent save(OutboxEvent event) {
        return repository.save(OutboxEventJpaEntity.fromDomain(event)).toDomain();
    }

    @Override
    public List<OutboxEvent> findUnpublished(int limit) {
        return repository.findByPublishedAtIsNullOrderByCreatedAtAsc(PageRequest.of(0, limit))
                .stream()
                .map(OutboxEventJpaEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void markPublished(UUID id) {
        repository.findById(id).ifPresent(entity -> {
            entity.markPublished(Instant.now());
            repository.save(entity);
        });
    }
}
