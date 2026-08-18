package com.example.securetenant.ledger.application;

import com.example.securetenant.ledger.domain.LedgerAccount;
import com.example.securetenant.ledger.domain.LedgerDirection;
import com.example.securetenant.ledger.domain.LedgerEntry;
import com.example.securetenant.payment.domain.Payment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public void postDoubleEntry(
            Payment payment,
            LedgerAccount debitAccount,
            LedgerAccount creditAccount
    ) {
        Instant now = Instant.now();
        LedgerEntry debit = new LedgerEntry(
                UUID.randomUUID(),
                payment.tenantId(),
                payment.id(),
                payment.walletId(),
                debitAccount,
                LedgerDirection.DEBIT,
                payment.amount(),
                payment.currency(),
                now
        );
        LedgerEntry credit = new LedgerEntry(
                UUID.randomUUID(),
                payment.tenantId(),
                payment.id(),
                payment.walletId(),
                creditAccount,
                LedgerDirection.CREDIT,
                payment.amount(),
                payment.currency(),
                now
        );
        ledgerEntryRepository.saveAll(List.of(debit, credit));
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> findByPayment(UUID paymentId) {
        return ledgerEntryRepository.findByPaymentId(paymentId);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> findAll() {
        return ledgerEntryRepository.findAll();
    }
}
