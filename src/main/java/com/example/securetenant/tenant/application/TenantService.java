package com.example.securetenant.tenant.application;

import com.example.securetenant.audit.application.AuditRecorder;
import com.example.securetenant.shared.api.ConflictException;
import com.example.securetenant.shared.api.ResourceNotFoundException;
import com.example.securetenant.tenant.domain.Tenant;
import com.example.securetenant.tenant.domain.TenantStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final AuditRecorder auditRecorder;

    public TenantService(TenantRepository tenantRepository, AuditRecorder auditRecorder) {
        this.tenantRepository = tenantRepository;
        this.auditRecorder = auditRecorder;
    }

    @Transactional
    public Tenant create(CreateTenantCommand command) {
        String identifier = command.identifier().trim().toLowerCase(Locale.ROOT);
        if (tenantRepository.existsByIdentifier(identifier)) {
            throw new ConflictException("Tenant already exists");
        }
        Tenant tenant = new Tenant(
                UUID.randomUUID(),
                identifier,
                command.name().trim(),
                TenantStatus.ACTIVE,
                null,
                null);
        Tenant saved = tenantRepository.save(tenant);
        auditRecorder.record("TENANT_CREATED", "TENANT", saved.id().toString(), saved.identifier());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Tenant> list() {
        return tenantRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Tenant getByIdentifier(String identifier) {
        return tenantRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
    }

    @Transactional
    public Tenant changeStatus(String identifier, TenantStatus status) {
        Tenant existing = getByIdentifier(identifier);
        Tenant updated = new Tenant(
                existing.id(),
                existing.identifier(),
                existing.name(),
                status,
                existing.createdAt(),
                existing.updatedAt());
        Tenant saved = tenantRepository.save(updated);
        auditRecorder.record("TENANT_STATUS_CHANGED", "TENANT", saved.id().toString(), saved.identifier());
        return saved;
    }
}
