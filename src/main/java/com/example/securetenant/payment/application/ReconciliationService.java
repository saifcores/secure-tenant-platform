package com.example.securetenant.payment.application;

import com.example.securetenant.ledger.application.LedgerService;
import com.example.securetenant.ledger.domain.LedgerAccount;
import com.example.securetenant.ledger.domain.LedgerDirection;
import com.example.securetenant.ledger.domain.LedgerEntry;
import com.example.securetenant.payment.domain.Payment;
import com.example.securetenant.payment.domain.PaymentStatus;
import com.example.securetenant.payment.domain.Settlement;
import com.example.securetenant.wallet.application.WalletService;
import com.example.securetenant.wallet.domain.Wallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReconciliationService {

    private final WalletService walletService;
    private final LedgerService ledgerService;
    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;

    public ReconciliationService(
            WalletService walletService,
            LedgerService ledgerService,
            PaymentRepository paymentRepository,
            SettlementRepository settlementRepository
    ) {
        this.walletService = walletService;
        this.ledgerService = ledgerService;
        this.paymentRepository = paymentRepository;
        this.settlementRepository = settlementRepository;
    }

    @Transactional(readOnly = true)
    public ReconciliationReport report() {
        List<LedgerEntry> entries = ledgerService.findAll();
        List<WalletReconciliation> wallets = walletService.list().stream()
                .map(wallet -> reconcileWallet(wallet, entries))
                .toList();
        List<UUID> unbalancedPayments = unbalancedPaymentIds(entries);
        List<Payment> payments = paymentRepository.findAll();
        List<Settlement> settlements = settlementRepository.findAll();
        Map<UUID, Settlement> settlementByPayment = settlements.stream()
                .collect(Collectors.toMap(Settlement::paymentId, settlement -> settlement, (a, b) -> a));
        List<UUID> settledWithoutSettlement = payments.stream()
                .filter(payment -> payment.status() == PaymentStatus.SETTLED)
                .map(Payment::id)
                .filter(id -> !settlementByPayment.containsKey(id))
                .toList();
        List<UUID> orphanSettlements = settlements.stream()
                .map(Settlement::paymentId)
                .filter(paymentId -> payments.stream().noneMatch(payment -> payment.id().equals(paymentId)))
                .toList();
        boolean balanced = wallets.stream().allMatch(WalletReconciliation::reservedMatches)
                && unbalancedPayments.isEmpty()
                && settledWithoutSettlement.isEmpty()
                && orphanSettlements.isEmpty();
        return new ReconciliationReport(
                balanced,
                wallets,
                unbalancedPayments,
                settledWithoutSettlement,
                orphanSettlements
        );
    }

    private WalletReconciliation reconcileWallet(Wallet wallet, List<LedgerEntry> entries) {
        BigDecimal reservedNet = entries.stream()
                .filter(entry -> wallet.id().equals(entry.walletId()))
                .filter(entry -> entry.account() == LedgerAccount.WALLET_RESERVED)
                .map(this::signedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean reservedMatches = wallet.reservedBalance().compareTo(reservedNet) == 0;
        return new WalletReconciliation(
                wallet.id(),
                wallet.currency(),
                wallet.availableBalance(),
                wallet.reservedBalance(),
                reservedNet,
                reservedMatches
        );
    }

    private BigDecimal signedAmount(LedgerEntry entry) {
        return entry.direction() == LedgerDirection.CREDIT ? entry.amount() : entry.amount().negate();
    }

    private List<UUID> unbalancedPaymentIds(List<LedgerEntry> entries) {
        Map<UUID, List<LedgerEntry>> byPayment = entries.stream()
                .filter(entry -> entry.paymentId() != null)
                .collect(Collectors.groupingBy(LedgerEntry::paymentId));
        List<UUID> unbalanced = new ArrayList<>();
        byPayment.forEach((paymentId, paymentEntries) -> {
            BigDecimal debits = sum(paymentEntries, LedgerDirection.DEBIT);
            BigDecimal credits = sum(paymentEntries, LedgerDirection.CREDIT);
            if (debits.compareTo(credits) != 0) {
                unbalanced.add(paymentId);
            }
        });
        return unbalanced;
    }

    private BigDecimal sum(List<LedgerEntry> entries, LedgerDirection direction) {
        return entries.stream()
                .filter(entry -> entry.direction() == direction)
                .map(LedgerEntry::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public record ReconciliationReport(
            boolean balanced,
            List<WalletReconciliation> wallets,
            List<UUID> unbalancedPaymentIds,
            List<UUID> settledWithoutSettlement,
            List<UUID> orphanSettlements
    ) {
    }

    public record WalletReconciliation(
            UUID walletId,
            String currency,
            BigDecimal availableBalance,
            BigDecimal reservedBalance,
            BigDecimal ledgerReservedNet,
            boolean reservedMatches
    ) {
    }
}
