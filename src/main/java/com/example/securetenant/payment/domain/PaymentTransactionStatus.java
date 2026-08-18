package com.example.securetenant.payment.domain;

public enum PaymentTransactionStatus {
    STARTED,
    SUCCEEDED,
    RETRYABLE_FAILURE,
    TIMED_OUT,
    FAILED
}
