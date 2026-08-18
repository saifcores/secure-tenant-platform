package com.example.securetenant.payment.application;

import com.example.securetenant.payment.domain.PaymentTransaction;

import java.util.List;
import java.util.UUID;

public interface PaymentTransactionRepository {

    PaymentTransaction save(PaymentTransaction transaction);

    List<PaymentTransaction> findByPaymentId(UUID paymentId);
}
