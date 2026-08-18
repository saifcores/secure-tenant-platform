package com.example.securetenant.shared.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class PlatformMetrics {

    private final Counter ordersCreated;
    private final Counter ordersFailed;
    private final Counter securityDenied;
    private final Counter tenantRequests;
    private final Counter paymentsCreated;
    private final Counter paymentsSettled;
    private final Counter paymentsFailed;
    private final Counter paymentsRetried;

    public PlatformMetrics(MeterRegistry registry) {
        this.ordersCreated = Counter.builder("orders_created_total")
                .description("Orders successfully created")
                .register(registry);
        this.ordersFailed = Counter.builder("orders_failed_total")
                .description("Order operations that failed business or persistence rules")
                .register(registry);
        this.securityDenied = Counter.builder("security_denied_total")
                .description("Authentication and authorization failures")
                .register(registry);
        this.tenantRequests = Counter.builder("tenant_requests_total")
                .description("Requests processed in a tenant context")
                .register(registry);
        this.paymentsCreated = Counter.builder("payments_created_total")
                .description("Payments initiated")
                .register(registry);
        this.paymentsSettled = Counter.builder("payments_settled_total")
                .description("Payments settled")
                .register(registry);
        this.paymentsFailed = Counter.builder("payments_failed_total")
                .description("Payments that failed permanently")
                .register(registry);
        this.paymentsRetried = Counter.builder("payments_retried_total")
                .description("Payment retry attempts scheduled or executed")
                .register(registry);
    }

    public void incrementOrdersCreated() {
        ordersCreated.increment();
    }

    public void incrementOrdersFailed() {
        ordersFailed.increment();
    }

    public void incrementSecurityDenied() {
        securityDenied.increment();
    }

    public void incrementTenantRequests() {
        tenantRequests.increment();
    }

    public void incrementPaymentsCreated() {
        paymentsCreated.increment();
    }

    public void incrementPaymentsSettled() {
        paymentsSettled.increment();
    }

    public void incrementPaymentsFailed() {
        paymentsFailed.increment();
    }

    public void incrementPaymentsRetried() {
        paymentsRetried.increment();
    }
}
