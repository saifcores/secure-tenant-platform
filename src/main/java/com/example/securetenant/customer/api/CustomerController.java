package com.example.securetenant.customer.api;

import com.example.securetenant.customer.application.CreateCustomerCommand;
import com.example.securetenant.customer.application.CustomerService;
import com.example.securetenant.customer.application.UpdateCustomerCommand;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Tenant-scoped CRM")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER','USER','AUDITOR')")
    public List<CustomerApi.CustomerResponse> list() {
        return customerService.list().stream().map(CustomerApi.CustomerResponse::from).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER','USER','AUDITOR')")
    public CustomerApi.CustomerResponse get(@PathVariable UUID id) {
        return CustomerApi.CustomerResponse.from(customerService.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER')")
    public CustomerApi.CustomerResponse create(@Valid @RequestBody CustomerApi.CreateCustomerRequest request) {
        return CustomerApi.CustomerResponse.from(customerService.create(
                new CreateCustomerCommand(request.name(), request.email(), request.phone())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER')")
    public CustomerApi.CustomerResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerApi.UpdateCustomerRequest request) {
        return CustomerApi.CustomerResponse.from(customerService.update(
                id,
                new UpdateCustomerCommand(request.name(), request.email(), request.phone())));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public void delete(@PathVariable UUID id) {
        customerService.delete(id);
    }
}
