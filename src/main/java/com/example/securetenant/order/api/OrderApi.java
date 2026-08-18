package com.example.securetenant.order.api;

import com.example.securetenant.order.domain.Order;
import com.example.securetenant.order.domain.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class OrderApi {

    private OrderApi() {
    }

    public record CreateOrderRequest(
            @NotNull UUID customerId,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotBlank @Size(min = 3, max = 3) String currency) {
    }

    public record OrderResponse(
            UUID id,
            String tenantId,
            UUID customerId,
            BigDecimal amount,
            String currency,
            OrderStatus status,
            Instant createdAt,
            Instant updatedAt) {
        public static OrderResponse from(Order order) {
            return new OrderResponse(
                    order.id(),
                    order.tenantId(),
                    order.customerId(),
                    order.amount(),
                    order.currency(),
                    order.status(),
                    order.createdAt(),
                    order.updatedAt());
        }
    }

    public record TenantStatsResponse(String tenantId, long orders, long customers) {
    }
}
