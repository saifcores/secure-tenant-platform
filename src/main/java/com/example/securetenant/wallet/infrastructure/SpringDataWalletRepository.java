package com.example.securetenant.wallet.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataWalletRepository extends JpaRepository<WalletJpaEntity, UUID> {

    Optional<WalletJpaEntity> findByCurrency(String currency);
}
