package com.example.securetenant.payment.infrastructure;

import com.example.securetenant.payment.application.PaymentRepository;
import com.example.securetenant.payment.domain.Payment;
import com.example.securetenant.payment.domain.PaymentStatus;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final SpringDataPaymentRepository repository;

    public PaymentRepositoryAdapter(SpringDataPaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = repository.findById(payment.id())
                .orElseGet(() -> PaymentJpaEntity.fromDomain(payment));
        entity.apply(payment);
        return repository.save(entity).toDomain();
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return repository.findById(id).map(PaymentJpaEntity::toDomain);
    }

    @Override
    public List<Payment> findAll() {
        return repository.findAll().stream().map(PaymentJpaEntity::toDomain).toList();
    }

    @Override
    public List<Payment> findDueForRetry(Instant now, int maxAttempts) {
        return repository.findByStatusInAndNextRetryAtLessThanEqualAndAttemptCountLessThan(
                        List.of(PaymentStatus.CREATED, PaymentStatus.AUTHORIZED),
                        now,
                        maxAttempts
                ).stream()
                .map(PaymentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsOpenForOrder(UUID orderId) {
        return repository.existsByOrderIdAndStatusIn(
                orderId,
                List.of(
                        PaymentStatus.CREATED,
                        PaymentStatus.AUTHORIZED,
                        PaymentStatus.CAPTURED,
                        PaymentStatus.SETTLED
                )
        );
    }
}
