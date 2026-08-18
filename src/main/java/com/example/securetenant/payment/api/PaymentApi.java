package com.example.securetenant.payment.api;

import com.example.securetenant.ledger.domain.LedgerAccount;
import com.example.securetenant.ledger.domain.LedgerDirection;
import com.example.securetenant.ledger.domain.LedgerEntry;
import com.example.securetenant.payment.application.ReconciliationService;
import com.example.securetenant.payment.domain.Payment;
import com.example.securetenant.payment.domain.PaymentStatus;
import com.example.securetenant.payment.domain.PaymentTransaction;
import com.example.securetenant.payment.domain.PaymentTransactionStatus;
import com.example.securetenant.payment.domain.Settlement;
import com.example.securetenant.payment.domain.SettlementStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class PaymentApi {

    private PaymentApi() {
    }

    public record CreatePaymentRequest(@NotNull UUID orderId) {
    }

    public record PaymentResponse(
            UUID id,
            String tenantId,
            UUID orderId,
            UUID walletId,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            String failureReason,
            int attemptCount,
            Instant nextRetryAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static PaymentResponse from(Payment payment) {
            return new PaymentResponse(
                    payment.id(),
                    payment.tenantId(),
                    payment.orderId(),
                    payment.walletId(),
                    payment.amount(),
                    payment.currency(),
                    payment.status(),
                    payment.failureReason(),
                    payment.attemptCount(),
                    payment.nextRetryAt(),
                    payment.createdAt(),
                    payment.updatedAt()
            );
        }
    }

    public record PaymentTransactionResponse(
            UUID id,
            UUID paymentId,
            int attempt,
            String pspReference,
            PaymentTransactionStatus status,
            String errorCode,
            String errorMessage,
            Instant createdAt
    ) {
        public static PaymentTransactionResponse from(PaymentTransaction transaction) {
            return new PaymentTransactionResponse(
                    transaction.id(),
                    transaction.paymentId(),
                    transaction.attempt(),
                    transaction.pspReference(),
                    transaction.status(),
                    transaction.errorCode(),
                    transaction.errorMessage(),
                    transaction.createdAt()
            );
        }
    }

    public record LedgerEntryResponse(
            UUID id,
            UUID paymentId,
            UUID walletId,
            LedgerAccount account,
            LedgerDirection direction,
            BigDecimal amount,
            String currency,
            Instant createdAt
    ) {
        public static LedgerEntryResponse from(LedgerEntry entry) {
            return new LedgerEntryResponse(
                    entry.id(),
                    entry.paymentId(),
                    entry.walletId(),
                    entry.account(),
                    entry.direction(),
                    entry.amount(),
                    entry.currency(),
                    entry.createdAt()
            );
        }
    }

    public record SettlementResponse(
            UUID id,
            String tenantId,
            UUID paymentId,
            BigDecimal amount,
            String currency,
            SettlementStatus status,
            Instant createdAt
    ) {
        public static SettlementResponse from(Settlement settlement) {
            return new SettlementResponse(
                    settlement.id(),
                    settlement.tenantId(),
                    settlement.paymentId(),
                    settlement.amount(),
                    settlement.currency(),
                    settlement.status(),
                    settlement.createdAt()
            );
        }
    }

    public record ReconciliationResponse(
            boolean balanced,
            List<ReconciliationService.WalletReconciliation> wallets,
            List<UUID> unbalancedPaymentIds,
            List<UUID> settledWithoutSettlement,
            List<UUID> orphanSettlements
    ) {
        public static ReconciliationResponse from(ReconciliationService.ReconciliationReport report) {
            return new ReconciliationResponse(
                    report.balanced(),
                    report.wallets(),
                    report.unbalancedPaymentIds(),
                    report.settledWithoutSettlement(),
                    report.orphanSettlements()
            );
        }
    }
}
