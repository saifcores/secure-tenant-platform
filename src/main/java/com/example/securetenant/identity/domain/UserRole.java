package com.example.securetenant.identity.domain;

public enum UserRole {
    PLATFORM_ADMIN,
    TENANT_ADMIN,
    MANAGER,
    USER,
    AUDITOR;

    public String authority() {
        return "ROLE_" + name();
    }

    public static UserRole fromAuthority(String authority) {
        String value = authority.startsWith("ROLE_") ? authority.substring(5) : authority;
        return UserRole.valueOf(value);
    }
}
