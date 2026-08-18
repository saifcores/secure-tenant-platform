package com.example.securetenant.outbox.infrastructure;

import com.example.securetenant.outbox.application.OutboxEventRepository;
import com.example.securetenant.outbox.application.OutboxRecorder;
import com.example.securetenant.outbox.domain.OutboxEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class OutboxRecorderAdapter implements OutboxRecorder {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxRecorderAdapter(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Override
    public void record(
            String tenantId,
            String aggregateType,
            String aggregateId,
            String eventType,
            Map<String, Object> payload
    ) {
        outboxEventRepository.save(new OutboxEvent(
                UUID.randomUUID(),
                tenantId,
                aggregateType,
                aggregateId,
                eventType,
                payload,
                Instant.now(),
                null
        ));
    }
}
