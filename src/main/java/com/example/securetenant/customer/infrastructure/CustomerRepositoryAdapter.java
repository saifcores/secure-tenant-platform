package com.example.securetenant.customer.infrastructure;

import com.example.securetenant.customer.application.CustomerRepository;
import com.example.securetenant.customer.domain.Customer;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final SpringDataCustomerRepository repository;

    public CustomerRepositoryAdapter(SpringDataCustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity entity = repository.findById(customer.id())
                .orElseGet(() -> CustomerJpaEntity.fromDomain(customer));
        entity.apply(customer);
        return repository.save(entity).toDomain();
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return repository.findById(id).map(CustomerJpaEntity::toDomain);
    }

    @Override
    public List<Customer> findAll() {
        return repository.findAll().stream().map(CustomerJpaEntity::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }
}
