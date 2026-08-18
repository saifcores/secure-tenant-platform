package com.example.securetenant.payment.infrastructure;

import com.example.securetenant.payment.application.DuePaymentScanner;
import com.example.securetenant.payment.application.PaymentOrchestrator;
import io.arconia.multitenancy.core.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@ConditionalOnProperty(name = "app.payments.retry.enabled", havingValue = "true")
public class PaymentRetryJob {

    private static final Logger log = LoggerFactory.getLogger(PaymentRetryJob.class);

    private final DuePaymentScanner duePaymentScanner;
    private final PaymentOrchestrator paymentOrchestrator;
    private final int maxAttempts;

    public PaymentRetryJob(
            DuePaymentScanner duePaymentScanner,
            PaymentOrchestrator paymentOrchestrator,
            @Value("${app.payments.psp.max-attempts:3}") int maxAttempts
    ) {
        this.duePaymentScanner = duePaymentScanner;
        this.paymentOrchestrator = paymentOrchestrator;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelayString = "${app.payments.retry.interval:15s}")
    public void retryDuePayments() {
        duePaymentScanner.findDue(Instant.now(), maxAttempts).forEach(ref ->
                TenantContext.where(ref.tenantId()).run(() -> {
                    try {
                        paymentOrchestrator.retry(ref.id());
                    } catch (RuntimeException ex) {
                        log.warn("Payment retry failed id={} tenant={}: {}",
                                ref.id(), ref.tenantId(), ex.getMessage());
                    }
                }));
    }
}
