package com.example.securetenant.customer.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, UUID> {

    boolean existsByEmailIgnoreCase(String email);
}
