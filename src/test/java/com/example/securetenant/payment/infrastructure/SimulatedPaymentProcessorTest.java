package com.example.securetenant.payment.infrastructure;

import com.example.securetenant.payment.application.PspResult;
import com.example.securetenant.payment.domain.Payment;
import com.example.securetenant.payment.domain.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SimulatedPaymentProcessorTest {

    private final SimulatedPaymentProcessor processor = new SimulatedPaymentProcessor();

    @Test
    void amountEndingWith99IsRetryable() {
        PspResult result = processor.authorize(payment("10.99"), Duration.ofSeconds(1));
        assertThat(result.success()).isFalse();
        assertThat(result.retryable()).isTrue();
        assertThat(result.errorCode()).isEqualTo("PSP_UNAVAILABLE");
    }

    @Test
    void amountEndingWith07IsHardDecline() {
        PspResult result = processor.authorize(payment("10.07"), Duration.ofSeconds(1));
        assertThat(result.success()).isFalse();
        assertThat(result.retryable()).isFalse();
        assertThat(result.errorCode()).isEqualTo("CARD_DECLINED");
    }

    @Test
    void amountEndingWith13TimesOut() {
        PspResult result = processor.authorize(payment("10.13"), Duration.ofMillis(50));
        assertThat(result.timedOut()).isTrue();
        assertThat(result.retryable()).isTrue();
    }

    @Test
    void otherAmountsSucceed() {
        PspResult result = processor.authorize(payment("12.50"), Duration.ofSeconds(1));
        assertThat(result.success()).isTrue();
        assertThat(result.pspReference()).startsWith("psp_");
    }

    private static Payment payment(String amount) {
        return new Payment(
                UUID.randomUUID(),
                "acme",
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal(amount),
                "USD",
                PaymentStatus.CREATED,
                null,
                0,
                null,
                null,
                null
        );
    }
}
