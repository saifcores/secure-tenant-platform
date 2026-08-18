package com.example.securetenant.payment.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface DuePaymentScanner {

    List<DuePaymentRef> findDue(Instant now, int maxAttempts);

    record DuePaymentRef(UUID id, String tenantId) {
    }
}
