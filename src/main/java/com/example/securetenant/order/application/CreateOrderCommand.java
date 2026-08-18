package com.example.securetenant.order.application;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderCommand(UUID customerId, BigDecimal amount, String currency) {
}
