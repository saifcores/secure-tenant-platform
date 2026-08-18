package com.example.securetenant.wallet.infrastructure;

import com.example.securetenant.wallet.application.WalletRepository;
import com.example.securetenant.wallet.domain.Wallet;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class WalletRepositoryAdapter implements WalletRepository {

    private final SpringDataWalletRepository repository;

    public WalletRepositoryAdapter(SpringDataWalletRepository repository) {
        this.repository = repository;
    }

    @Override
    public Wallet save(Wallet wallet) {
        WalletJpaEntity entity = repository.findById(wallet.id())
                .orElseGet(() -> WalletJpaEntity.fromDomain(wallet));
        entity.apply(wallet);
        return repository.save(entity).toDomain();
    }

    @Override
    public Optional<Wallet> findById(UUID id) {
        return repository.findById(id).map(WalletJpaEntity::toDomain);
    }

    @Override
    public Optional<Wallet> findByCurrency(String currency) {
        return repository.findByCurrency(currency).map(WalletJpaEntity::toDomain);
    }

    @Override
    public List<Wallet> findAll() {
        return repository.findAll().stream().map(WalletJpaEntity::toDomain).toList();
    }
}
