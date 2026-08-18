package com.example.securetenant.payment.application;

import com.example.securetenant.payment.domain.Payment;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID id);

    List<Payment> findAll();

    List<Payment> findDueForRetry(Instant now, int maxAttempts);

    boolean existsOpenForOrder(UUID orderId);
}
