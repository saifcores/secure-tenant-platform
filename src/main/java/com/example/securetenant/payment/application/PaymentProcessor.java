package com.example.securetenant.payment.application;

import com.example.securetenant.payment.domain.Payment;

import java.time.Duration;

public interface PaymentProcessor {

    PspResult authorize(Payment payment, Duration timeout);
}
