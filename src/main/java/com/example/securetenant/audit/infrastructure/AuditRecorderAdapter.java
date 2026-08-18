package com.example.securetenant.audit.infrastructure;

import com.example.securetenant.audit.application.AuditRecorder;
import com.example.securetenant.audit.domain.AuditEvent;
import com.example.securetenant.identity.domain.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class AuditRecorderAdapter implements AuditRecorder {

    private final AuditEventRepositoryAdapter repository;

    public AuditRecorderAdapter(AuditEventRepositoryAdapter repository) {
        this.repository = repository;
    }

    @Override
    public void record(String action, String resourceType, String resourceId, String tenantId) {
        String userId = AuthenticatedUser.from(SecurityContextHolder.getContext().getAuthentication())
                .map(AuthenticatedUser::subject)
                .orElse("anonymous");
        AuditEvent event = new AuditEvent(
                UUID.randomUUID(),
                tenantId,
                userId,
                action,
                resourceType,
                resourceId,
                Instant.now(),
                clientIp(),
                Map.of());
        repository.save(event);
    }

    private String clientIp() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return null;
    }
}
