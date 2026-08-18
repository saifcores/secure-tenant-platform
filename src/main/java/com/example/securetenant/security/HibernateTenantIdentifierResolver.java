package com.example.securetenant.security;

import io.arconia.multitenancy.core.context.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class HibernateTenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    static final String UNRESOLVED = "unresolved";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantIdentifier();
        return tenantId == null || tenantId.isBlank() ? UNRESOLVED : tenantId;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
