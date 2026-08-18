package com.example.securetenant.tenant.api;

import com.example.securetenant.tenant.domain.Tenant;
import com.example.securetenant.tenant.domain.TenantStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class TenantApi {

    private TenantApi() {
    }

    public record CreateTenantRequest(
            @NotBlank @Size(max = 64) @Pattern(regexp = "^[a-z0-9][a-z0-9_-]{1,62}$") String identifier,
            @NotBlank @Size(max = 255) String name) {
    }

    public record ChangeStatusRequest(@NotBlank String status) {
        public TenantStatus toStatus() {
            return TenantStatus.valueOf(status.trim().toUpperCase());
        }
    }

    public record TenantResponse(
            UUID id,
            String identifier,
            String name,
            TenantStatus status,
            Instant createdAt,
            Instant updatedAt) {
        public static TenantResponse from(Tenant tenant) {
            return new TenantResponse(
                    tenant.id(),
                    tenant.identifier(),
                    tenant.name(),
                    tenant.status(),
                    tenant.createdAt(),
                    tenant.updatedAt());
        }
    }
}
