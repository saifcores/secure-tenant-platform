package com.example.securetenant.outbox.application;

import com.example.securetenant.outbox.domain.OutboxEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface OutboxEventRepository {

    OutboxEvent save(OutboxEvent event);

    List<OutboxEvent> findUnpublished(int limit);

    void markPublished(UUID id);
}
