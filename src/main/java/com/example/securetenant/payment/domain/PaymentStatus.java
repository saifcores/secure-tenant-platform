package com.example.securetenant.payment.domain;

public enum PaymentStatus {
    CREATED,
    AUTHORIZED,
    CAPTURED,
    SETTLED,
    FAILED,
    CANCELLED
}
