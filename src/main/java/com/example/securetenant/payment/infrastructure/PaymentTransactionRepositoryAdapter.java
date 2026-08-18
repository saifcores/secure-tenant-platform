package com.example.securetenant.payment.infrastructure;

import com.example.securetenant.payment.application.PaymentTransactionRepository;
import com.example.securetenant.payment.domain.PaymentTransaction;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class PaymentTransactionRepositoryAdapter implements PaymentTransactionRepository {

    private final SpringDataPaymentTransactionRepository repository;

    public PaymentTransactionRepositoryAdapter(SpringDataPaymentTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public PaymentTransaction save(PaymentTransaction transaction) {
        return repository.save(PaymentTransactionJpaEntity.fromDomain(transaction)).toDomain();
    }

    @Override
    public List<PaymentTransaction> findByPaymentId(UUID paymentId) {
        return repository.findByPaymentIdOrderByAttemptAsc(paymentId).stream()
                .map(PaymentTransactionJpaEntity::toDomain)
                .toList();
    }
}
