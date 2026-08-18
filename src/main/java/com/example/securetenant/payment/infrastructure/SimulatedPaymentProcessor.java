package com.example.securetenant.payment.infrastructure;

import com.example.securetenant.payment.application.PaymentProcessor;
import com.example.securetenant.payment.application.PspResult;
import com.example.securetenant.payment.domain.Payment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class SimulatedPaymentProcessor implements PaymentProcessor {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public PspResult authorize(Payment payment, Duration timeout) {
        var future = executor.submit(() -> simulate(payment));
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            return PspResult.timeout();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return PspResult.timeout();
        } catch (ExecutionException ex) {
            return PspResult.failed("PSP_ERROR", "PSP call failed");
        }
    }

    private PspResult simulate(Payment payment) throws InterruptedException {
        int cents = payment.amount().movePointRight(2).intValue();
        int suffix = cents % 100;
        if (suffix == 13) {
            Thread.sleep(10_000);
        }
        if (suffix == 99) {
            return PspResult.retryable("PSP_UNAVAILABLE", "Simulated PSP outage");
        }
        if (suffix == 7) {
            return PspResult.failed("CARD_DECLINED", "Simulated hard decline");
        }
        return PspResult.succeeded("psp_" + UUID.randomUUID().toString().substring(0, 8));
    }
}
