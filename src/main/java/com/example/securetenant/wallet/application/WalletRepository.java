package com.example.securetenant.wallet.application;

import com.example.securetenant.wallet.domain.Wallet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {

    Wallet save(Wallet wallet);

    Optional<Wallet> findById(UUID id);

    Optional<Wallet> findByCurrency(String currency);

    List<Wallet> findAll();
}
