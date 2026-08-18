package com.example.securetenant.outbox.application;

import java.util.Map;

public interface OutboxRecorder {

    void record(String tenantId, String aggregateType, String aggregateId, String eventType, Map<String, Object> payload);
}
