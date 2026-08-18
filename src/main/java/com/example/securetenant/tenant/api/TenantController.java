package com.example.securetenant.tenant.api;

import com.example.securetenant.tenant.application.CreateTenantCommand;
import com.example.securetenant.tenant.application.TenantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public List<TenantApi.TenantResponse> list() {
        return tenantService.list().stream().map(TenantApi.TenantResponse::from).toList();
    }

    @GetMapping("/{identifier}")
    public TenantApi.TenantResponse get(@PathVariable String identifier) {
        return TenantApi.TenantResponse.from(tenantService.getByIdentifier(identifier));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantApi.TenantResponse create(@Valid @RequestBody TenantApi.CreateTenantRequest request) {
        return TenantApi.TenantResponse.from(
                tenantService.create(new CreateTenantCommand(request.identifier(), request.name())));
    }

    @PutMapping("/{identifier}/status")
    public TenantApi.TenantResponse changeStatus(
            @PathVariable String identifier,
            @Valid @RequestBody TenantApi.ChangeStatusRequest request) {
        return TenantApi.TenantResponse.from(tenantService.changeStatus(identifier, request.toStatus()));
    }
}
