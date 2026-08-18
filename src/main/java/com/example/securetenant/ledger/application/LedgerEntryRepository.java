package com.example.securetenant.ledger.application;

import com.example.securetenant.ledger.domain.LedgerEntry;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository {

    void saveAll(List<LedgerEntry> entries);

    List<LedgerEntry> findByPaymentId(UUID paymentId);

    List<LedgerEntry> findByWalletId(UUID walletId);

    List<LedgerEntry> findAll();
}
