package com.example.securetenant.audit.application;

public interface AuditRecorder {

    void record(String action, String resourceType, String resourceId, String tenantId);
}
