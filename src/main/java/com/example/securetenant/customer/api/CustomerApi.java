package com.example.securetenant.customer.api;

import com.example.securetenant.customer.domain.Customer;
import com.example.securetenant.customer.domain.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class CustomerApi {

        private CustomerApi() {
        }

        public record CreateCustomerRequest(
                        @NotBlank @Size(max = 255) String name,
                        @NotBlank @Email String email,
                        @Size(max = 64) String phone) {
        }

        public record UpdateCustomerRequest(
                        @NotBlank @Size(max = 255) String name,
                        @NotBlank @Email String email,
                        @Size(max = 64) String phone) {
        }

        public record CustomerResponse(
                        UUID id,
                        String tenantId,
                        String name,
                        String email,
                        String phone,
                        CustomerStatus status,
                        Instant createdAt,
                        Instant updatedAt) {
                public static CustomerResponse from(Customer customer) {
                        return new CustomerResponse(
                                        customer.id(),
                                        customer.tenantId(),
                                        customer.name(),
                                        customer.email(),
                                        customer.phone(),
                                        customer.status(),
                                        customer.createdAt(),
                                        customer.updatedAt());
                }
        }
}
