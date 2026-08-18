package com.example.securetenant.payment.application;

import com.example.securetenant.audit.application.AuditRecorder;
import com.example.securetenant.ledger.application.LedgerService;
import com.example.securetenant.ledger.domain.LedgerAccount;
import com.example.securetenant.order.application.OrderRepository;
import com.example.securetenant.order.application.OrderService;
import com.example.securetenant.order.domain.Order;
import com.example.securetenant.order.domain.OrderStatus;
import com.example.securetenant.outbox.application.OutboxRecorder;
import com.example.securetenant.payment.domain.Payment;
import com.example.securetenant.payment.domain.PaymentStateMachine;
import com.example.securetenant.payment.domain.PaymentStatus;
import com.example.securetenant.payment.domain.PaymentTransaction;
import com.example.securetenant.payment.domain.PaymentTransactionStatus;
import com.example.securetenant.payment.domain.Settlement;
import com.example.securetenant.payment.domain.SettlementStatus;
import com.example.securetenant.security.CurrentTenant;
import com.example.securetenant.shared.api.BusinessRuleException;
import com.example.securetenant.shared.api.ResourceNotFoundException;
import com.example.securetenant.shared.observability.PlatformMetrics;
import com.example.securetenant.wallet.application.WalletService;
import com.example.securetenant.wallet.domain.Wallet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentOrchestrator {

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final SettlementRepository settlementRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final WalletService walletService;
    private final LedgerService ledgerService;
    private final PaymentProcessor paymentProcessor;
    private final OutboxRecorder outboxRecorder;
    private final AuditRecorder auditRecorder;
    private final PlatformMetrics platformMetrics;
    private final Duration pspTimeout;
    private final int maxAttempts;
    private final Duration retryDelay;

    public PaymentOrchestrator(
            PaymentRepository paymentRepository,
            PaymentTransactionRepository transactionRepository,
            SettlementRepository settlementRepository,
            OrderRepository orderRepository,
            OrderService orderService,
            WalletService walletService,
            LedgerService ledgerService,
            PaymentProcessor paymentProcessor,
            OutboxRecorder outboxRecorder,
            AuditRecorder auditRecorder,
            PlatformMetrics platformMetrics,
            @Value("${app.payments.psp.timeout:2s}") Duration pspTimeout,
            @Value("${app.payments.psp.max-attempts:3}") int maxAttempts,
            @Value("${app.payments.retry.delay:10s}") Duration retryDelay
    ) {
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
        this.settlementRepository = settlementRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.walletService = walletService;
        this.ledgerService = ledgerService;
        this.paymentProcessor = paymentProcessor;
        this.outboxRecorder = outboxRecorder;
        this.auditRecorder = auditRecorder;
        this.platformMetrics = platformMetrics;
        this.pspTimeout = pspTimeout;
        this.maxAttempts = maxAttempts;
        this.retryDelay = retryDelay;
    }

    @Transactional(readOnly = true)
    public List<Payment> list() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Payment get(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    @Transactional
    public Payment initiate(CreatePaymentCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.status() != OrderStatus.CONFIRMED) {
            throw new BusinessRuleException("Only CONFIRMED orders can be paid");
        }
        if (paymentRepository.existsOpenForOrder(order.id())) {
            throw new BusinessRuleException("Order already has an active payment");
        }
        Wallet wallet = walletService.requireByCurrency(order.currency());
        Payment payment = new Payment(
                UUID.randomUUID(),
                CurrentTenant.require(),
                order.id(),
                wallet.id(),
                order.amount(),
                order.currency(),
                PaymentStatus.CREATED,
                null,
                0,
                null,
                null,
                null
        );
        Payment saved = paymentRepository.save(payment);
        walletService.reserve(wallet, saved.amount());
        ledgerService.postDoubleEntry(saved, LedgerAccount.WALLET_AVAILABLE, LedgerAccount.WALLET_RESERVED);
        auditRecorder.record("PAYMENT_CREATED", "PAYMENT", saved.id().toString(), saved.tenantId());
        outboxRecorder.record(
                saved.tenantId(),
                "PAYMENT",
                saved.id().toString(),
                "PAYMENT_CREATED",
                Map.of("paymentId", saved.id().toString(), "amount", saved.amount(), "currency", saved.currency())
        );
        platformMetrics.incrementPaymentsCreated();
        return processWithPsp(saved);
    }

    @Transactional
    public Payment retry(UUID paymentId) {
        Payment payment = get(paymentId);
        if (!PaymentStateMachine.isRetryable(payment.status())) {
            throw new BusinessRuleException("Payment is not retryable");
        }
        return processWithPsp(payment);
    }

    @Transactional
    public Payment cancel(UUID paymentId) {
        Payment payment = get(paymentId);
        if (!PaymentStateMachine.isCancellable(payment.status())) {
            throw new BusinessRuleException("Payment cannot be cancelled");
        }
        Wallet wallet = walletService.requireByCurrency(payment.currency());
        walletService.release(wallet, payment.amount());
        ledgerService.postDoubleEntry(payment, LedgerAccount.WALLET_RESERVED, LedgerAccount.WALLET_AVAILABLE);
        Payment cancelled = saveTransition(
                payment,
                PaymentStatus.CANCELLED,
                payment.attemptCount(),
                "Cancelled by user"
        );
        auditRecorder.record("PAYMENT_CANCELLED", "PAYMENT", cancelled.id().toString(), cancelled.tenantId());
        outboxRecorder.record(
                cancelled.tenantId(),
                "PAYMENT",
                cancelled.id().toString(),
                "PAYMENT_CANCELLED",
                Map.of("paymentId", cancelled.id().toString(), "orderId", cancelled.orderId().toString())
        );
        return cancelled;
    }

    @Transactional(readOnly = true)
    public List<PaymentTransaction> transactions(UUID paymentId) {
        get(paymentId);
        return transactionRepository.findByPaymentId(paymentId);
    }

    @Transactional(readOnly = true)
    public List<Settlement> settlements() {
        return settlementRepository.findAll();
    }

    private Payment processWithPsp(Payment payment) {
        int attempt = payment.attemptCount() + 1;
        PspResult result = paymentProcessor.authorize(payment, pspTimeout);
        PaymentTransactionStatus txStatus = toTransactionStatus(result);
        transactionRepository.save(new PaymentTransaction(
                UUID.randomUUID(),
                payment.tenantId(),
                payment.id(),
                attempt,
                result.pspReference(),
                txStatus,
                result.errorCode(),
                result.errorMessage(),
                Instant.now(),
                Instant.now()
        ));
        if (result.success()) {
            return settle(payment, attempt);
        }
        if (result.retryable() && attempt < maxAttempts) {
            platformMetrics.incrementPaymentsRetried();
            Payment waiting = copy(
                    payment,
                    payment.status(),
                    result.errorMessage(),
                    attempt,
                    Instant.now().plus(retryDelay)
            );
            Payment saved = paymentRepository.save(waiting);
            auditRecorder.record("PAYMENT_RETRY_SCHEDULED", "PAYMENT", saved.id().toString(), saved.tenantId());
            return saved;
        }
        return fail(payment, attempt, result.errorMessage());
    }

    private Payment settle(Payment payment, int attempt) {
        Payment authorized = saveTransition(payment, PaymentStatus.AUTHORIZED, attempt, null);
        Wallet wallet = walletService.requireByCurrency(authorized.currency());
        walletService.capture(wallet, authorized.amount());
        ledgerService.postDoubleEntry(authorized, LedgerAccount.WALLET_RESERVED, LedgerAccount.PSP_CLEARING);
        Payment captured = saveTransition(authorized, PaymentStatus.CAPTURED, attempt, null);
        ledgerService.postDoubleEntry(captured, LedgerAccount.PSP_CLEARING, LedgerAccount.SETTLEMENT);
        settlementRepository.save(new Settlement(
                UUID.randomUUID(),
                captured.tenantId(),
                captured.id(),
                captured.amount(),
                captured.currency(),
                SettlementStatus.COMPLETED,
                Instant.now(),
                Instant.now()
        ));
        Payment settled = saveTransition(captured, PaymentStatus.SETTLED, attempt, null);
        outboxRecorder.record(
                settled.tenantId(),
                "PAYMENT",
                settled.id().toString(),
                "PAYMENT_SETTLED",
                Map.of(
                        "paymentId", settled.id().toString(),
                        "orderId", settled.orderId().toString(),
                        "amount", settled.amount(),
                        "currency", settled.currency()
                )
        );
        auditRecorder.record("PAYMENT_SETTLED", "PAYMENT", settled.id().toString(), settled.tenantId());
        orderService.completeAfterSettlement(settled.orderId());
        platformMetrics.incrementPaymentsSettled();
        return settled;
    }

    private Payment fail(Payment payment, int attempt, String reason) {
        Wallet wallet = walletService.requireByCurrency(payment.currency());
        walletService.release(wallet, payment.amount());
        ledgerService.postDoubleEntry(payment, LedgerAccount.WALLET_RESERVED, LedgerAccount.WALLET_AVAILABLE);
        Payment failed = saveTransition(payment, PaymentStatus.FAILED, attempt, reason);
        auditRecorder.record("PAYMENT_FAILED", "PAYMENT", failed.id().toString(), failed.tenantId());
        outboxRecorder.record(
                failed.tenantId(),
                "PAYMENT",
                failed.id().toString(),
                "PAYMENT_FAILED",
                Map.of("paymentId", failed.id().toString(), "reason", reason == null ? "unknown" : reason)
        );
        platformMetrics.incrementPaymentsFailed();
        return failed;
    }

    private Payment saveTransition(Payment payment, PaymentStatus target, int attempt, String failureReason) {
        PaymentStatus next = PaymentStateMachine.transition(payment.status(), target);
        Payment updated = copy(payment, next, failureReason, attempt, null);
        return paymentRepository.save(updated);
    }

    private Payment copy(
            Payment payment,
            PaymentStatus status,
            String failureReason,
            int attempt,
            Instant nextRetryAt
    ) {
        return new Payment(
                payment.id(),
                payment.tenantId(),
                payment.orderId(),
                payment.walletId(),
                payment.amount(),
                payment.currency(),
                status,
                failureReason,
                attempt,
                nextRetryAt,
                payment.createdAt(),
                payment.updatedAt()
        );
    }

    private PaymentTransactionStatus toTransactionStatus(PspResult result) {
        if (result.success()) {
            return PaymentTransactionStatus.SUCCEEDED;
        }
        if (result.timedOut()) {
            return PaymentTransactionStatus.TIMED_OUT;
        }
        if (result.retryable()) {
            return PaymentTransactionStatus.RETRYABLE_FAILURE;
        }
        return PaymentTransactionStatus.FAILED;
    }
}
