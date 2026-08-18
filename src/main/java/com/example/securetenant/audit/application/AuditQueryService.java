package com.example.securetenant.audit.application;

import com.example.securetenant.audit.domain.AuditEvent;
import com.example.securetenant.identity.domain.AuthenticatedUser;
import com.example.securetenant.security.CurrentTenant;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditQueryService {

    private final AuditEventRepository auditEventRepository;

    public AuditQueryService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> listForCurrentPrincipal() {
        AuthenticatedUser user = AuthenticatedUser.from(SecurityContextHolder.getContext().getAuthentication())
                .orElseThrow();
        if (user.isPlatformAdmin()) {
            return auditEventRepository.findAll();
        }
        return auditEventRepository.findByTenantId(CurrentTenant.require());
    }
}
