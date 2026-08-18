package com.example.securetenant.idempotency.application;

import com.example.securetenant.idempotency.domain.IdempotencyKey;
import com.example.securetenant.payment.application.CreatePaymentCommand;
import com.example.securetenant.payment.application.PaymentRepository;
import com.example.securetenant.payment.domain.Payment;
import com.example.securetenant.security.CurrentTenant;
import com.example.securetenant.shared.api.BadRequestException;
import com.example.securetenant.shared.api.ConflictException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class IdempotentPaymentExecutor {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final PaymentRepository paymentRepository;

    public IdempotentPaymentExecutor(
            IdempotencyKeyRepository idempotencyKeyRepository,
            PaymentRepository paymentRepository
    ) {
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment execute(CreatePaymentCommand command, Supplier<Payment> action) {
        if (!StringUtils.hasText(command.idempotencyKey())) {
            throw new BadRequestException("Idempotency-Key header is required");
        }
        String tenantId = CurrentTenant.require();
        String key = command.idempotencyKey().trim();
        String hash = sha256(command.orderId().toString());
        return idempotencyKeyRepository.findByTenantIdAndKey(tenantId, key)
                .map(existing -> replayOrReject(existing, hash))
                .orElseGet(() -> start(tenantId, key, hash, action));
    }

    private Payment start(String tenantId, String key, String hash, Supplier<Payment> action) {
        IdempotencyKey pending = new IdempotencyKey(
                UUID.randomUUID(),
                tenantId,
                key,
                hash,
                null,
                null,
                Instant.now()
        );
        try {
            idempotencyKeyRepository.saveAndFlush(pending);
        } catch (DataIntegrityViolationException ex) {
            IdempotencyKey existing = idempotencyKeyRepository.findByTenantIdAndKey(tenantId, key)
                    .orElseThrow(() -> ex);
            return replayOrReject(existing, hash);
        }
        Payment payment = action.get();
        idempotencyKeyRepository.save(new IdempotencyKey(
                pending.id(),
                pending.tenantId(),
                pending.key(),
                pending.requestHash(),
                payment.id().toString(),
                201,
                pending.createdAt()
        ));
        return payment;
    }

    private Payment replayOrReject(IdempotencyKey existing, String hash) {
        if (!existing.requestHash().equals(hash)) {
            throw new ConflictException("Idempotency-Key reused with a different request");
        }
        if (existing.responseBody() == null || existing.responseBody().isBlank()) {
            throw new ConflictException("Idempotent request is already in progress");
        }
        UUID paymentId = UUID.fromString(existing.responseBody());
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ConflictException("Idempotent payment is no longer visible"));
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
