package com.example.securetenant.payment.application;

import java.util.UUID;

public record CreatePaymentCommand(UUID orderId, String idempotencyKey) {
}
