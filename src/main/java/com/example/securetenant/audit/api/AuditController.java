package com.example.securetenant.audit.api;

import com.example.securetenant.audit.application.AuditQueryService;
import com.example.securetenant.audit.domain.AuditEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditQueryService auditQueryService;

    public AuditController(AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','TENANT_ADMIN','AUDITOR')")
    public List<AuditEventResponse> list() {
        return auditQueryService.listForCurrentPrincipal().stream()
                .map(AuditEventResponse::from)
                .toList();
    }

    public record AuditEventResponse(
            UUID id,
            String tenantId,
            String userId,
            String action,
            String resourceType,
            String resourceId,
            Instant timestamp,
            String ipAddress,
            Map<String, Object> metadata) {
        static AuditEventResponse from(AuditEvent event) {
            return new AuditEventResponse(
                    event.id(),
                    event.tenantId(),
                    event.userId(),
                    event.action(),
                    event.resourceType(),
                    event.resourceId(),
                    event.timestamp(),
                    event.ipAddress(),
                    event.metadata());
        }
    }
}
