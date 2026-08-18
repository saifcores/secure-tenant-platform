package com.example.securetenant.customer.application;

import com.example.securetenant.audit.application.AuditRecorder;
import com.example.securetenant.customer.domain.Customer;
import com.example.securetenant.customer.domain.CustomerStatus;
import com.example.securetenant.security.CurrentTenant;
import com.example.securetenant.shared.api.ConflictException;
import com.example.securetenant.shared.api.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuditRecorder auditRecorder;

    public CustomerService(CustomerRepository customerRepository, AuditRecorder auditRecorder) {
        this.customerRepository = customerRepository;
        this.auditRecorder = auditRecorder;
    }

    @Transactional(readOnly = true)
    public List<Customer> list() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Customer get(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Transactional
    public Customer create(CreateCustomerCommand command) {
        if (customerRepository.existsByEmail(command.email())) {
            throw new ConflictException("Customer email already exists");
        }
        Customer customer = new Customer(
                UUID.randomUUID(),
                CurrentTenant.require(),
                command.name().trim(),
                command.email().trim().toLowerCase(),
                command.phone(),
                CustomerStatus.ACTIVE,
                null,
                null);
        Customer saved = customerRepository.save(customer);
        auditRecorder.record("CUSTOMER_CREATED", "CUSTOMER", saved.id().toString(), saved.tenantId());
        return saved;
    }

    @Transactional
    public Customer update(UUID id, UpdateCustomerCommand command) {
        Customer existing = get(id);
        Customer updated = new Customer(
                existing.id(),
                existing.tenantId(),
                command.name().trim(),
                command.email().trim().toLowerCase(),
                command.phone(),
                existing.status(),
                existing.createdAt(),
                existing.updatedAt());
        Customer saved = customerRepository.save(updated);
        auditRecorder.record("CUSTOMER_UPDATED", "CUSTOMER", saved.id().toString(), saved.tenantId());
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        Customer existing = get(id);
        customerRepository.deleteById(existing.id());
        auditRecorder.record("CUSTOMER_DELETED", "CUSTOMER", existing.id().toString(), existing.tenantId());
    }
}
