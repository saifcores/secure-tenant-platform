package com.example.securetenant.wallet.application;

import com.example.securetenant.shared.api.BusinessRuleException;
import com.example.securetenant.shared.api.ResourceNotFoundException;
import com.example.securetenant.wallet.domain.Wallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional(readOnly = true)
    public List<Wallet> list() {
        return walletRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Wallet requireByCurrency(String currency) {
        return walletRepository.findByCurrency(currency)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for currency " + currency));
    }

    @Transactional
    public Wallet reserve(Wallet wallet, BigDecimal amount) {
        if (wallet.availableBalance().compareTo(amount) < 0) {
            throw new BusinessRuleException("Insufficient available balance");
        }
        Wallet updated = new Wallet(
                wallet.id(),
                wallet.tenantId(),
                wallet.currency(),
                wallet.availableBalance().subtract(amount),
                wallet.reservedBalance().add(amount),
                wallet.createdAt(),
                wallet.updatedAt()
        );
        return walletRepository.save(updated);
    }

    @Transactional
    public Wallet capture(Wallet wallet, BigDecimal amount) {
        if (wallet.reservedBalance().compareTo(amount) < 0) {
            throw new BusinessRuleException("Insufficient reserved balance");
        }
        Wallet updated = new Wallet(
                wallet.id(),
                wallet.tenantId(),
                wallet.currency(),
                wallet.availableBalance(),
                wallet.reservedBalance().subtract(amount),
                wallet.createdAt(),
                wallet.updatedAt()
        );
        return walletRepository.save(updated);
    }

    @Transactional
    public Wallet release(Wallet wallet, BigDecimal amount) {
        if (wallet.reservedBalance().compareTo(amount) < 0) {
            throw new BusinessRuleException("Insufficient reserved balance");
        }
        Wallet updated = new Wallet(
                wallet.id(),
                wallet.tenantId(),
                wallet.currency(),
                wallet.availableBalance().add(amount),
                wallet.reservedBalance().subtract(amount),
                wallet.createdAt(),
                wallet.updatedAt()
        );
        return walletRepository.save(updated);
    }
}
