package com.example.securetenant.security;

import io.arconia.multitenancy.core.context.TenantContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentTenantTest {

    @Test
    void requireReturnsBoundTenant() {
        TenantContext.where("acme").run(() -> assertThat(CurrentTenant.require()).isEqualTo("acme"));
    }

    @Test
    void requireFailsWhenUnbound() {
        assertThatThrownBy(CurrentTenant::require).isInstanceOf(RuntimeException.class);
    }

    @Test
    void headerCannotOverrideBoundContext() {
        TenantContext.where("acme").run(() -> {
            String ignoredHeader = "globex";
            assertThat(CurrentTenant.require()).isEqualTo("acme");
            assertThat(CurrentTenant.require()).isNotEqualTo(ignoredHeader);
        });
    }
}
