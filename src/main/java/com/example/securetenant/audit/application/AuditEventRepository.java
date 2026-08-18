package com.example.securetenant.audit.application;

import com.example.securetenant.audit.domain.AuditEvent;

import java.util.List;

public interface AuditEventRepository {

    AuditEvent save(AuditEvent event);

    List<AuditEvent> findByTenantId(String tenantId);

    List<AuditEvent> findAll();
}
