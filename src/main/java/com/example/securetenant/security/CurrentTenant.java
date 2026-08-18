package com.example.securetenant.security;

import io.arconia.multitenancy.core.context.TenantContext;

public final class CurrentTenant {

    private CurrentTenant() {
    }

    public static String require() {
        return TenantContext.getRequiredTenantIdentifier();
    }
}
