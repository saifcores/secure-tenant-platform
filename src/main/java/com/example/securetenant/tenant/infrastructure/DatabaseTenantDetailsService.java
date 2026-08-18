package com.example.securetenant.tenant.infrastructure;

import com.example.securetenant.tenant.application.TenantRepository;
import com.example.securetenant.tenant.domain.Tenant;
import io.arconia.multitenancy.core.tenantdetails.TenantDetails;
import io.arconia.multitenancy.core.tenantdetails.TenantDetailsService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatabaseTenantDetailsService implements TenantDetailsService {

    private final TenantRepository tenantRepository;

    public DatabaseTenantDetailsService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public List<? extends TenantDetails> loadAllTenants() {
        return tenantRepository.findAll().stream().map(this::toDetails).toList();
    }

    @Override
    public TenantDetails loadTenantByIdentifier(String identifier) {
        return tenantRepository.findByIdentifier(identifier)
                .map(this::toDetails)
                .orElse(null);
    }

    private TenantDetails toDetails(Tenant tenant) {
        return new TenantDetails() {
            @Override
            public String identifier() {
                return tenant.identifier();
            }

            @Override
            public boolean enabled() {
                return tenant.isEnabled();
            }

            @Override
            public Map<String, Object> attributes() {
                return Map.of(
                        "name", tenant.name(),
                        "status", tenant.status().name());
            }
        };
    }
}
