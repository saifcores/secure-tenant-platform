package com.example.securetenant.wallet.api;

import com.example.securetenant.wallet.application.WalletService;
import com.example.securetenant.wallet.domain.Wallet;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER','USER','AUDITOR')")
    public List<WalletResponse> list() {
        return walletService.list().stream().map(WalletResponse::from).toList();
    }

    public record WalletResponse(
            UUID id,
            String tenantId,
            String currency,
            BigDecimal availableBalance,
            BigDecimal reservedBalance,
            BigDecimal totalBalance,
            Instant createdAt,
            Instant updatedAt
    ) {
        static WalletResponse from(Wallet wallet) {
            return new WalletResponse(
                    wallet.id(),
                    wallet.tenantId(),
                    wallet.currency(),
                    wallet.availableBalance(),
                    wallet.reservedBalance(),
                    wallet.totalBalance(),
                    wallet.createdAt(),
                    wallet.updatedAt()
            );
        }
    }
}
