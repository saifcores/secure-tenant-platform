package com.example.securetenant.customer.application;

import com.example.securetenant.customer.domain.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(UUID id);

    List<Customer> findAll();

    void deleteById(UUID id);

    boolean existsByEmail(String email);
}
