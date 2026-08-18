package com.example.securetenant.payment.application;

import com.example.securetenant.payment.domain.Settlement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementRepository {

    Settlement save(Settlement settlement);

    Optional<Settlement> findByPaymentId(UUID paymentId);

    List<Settlement> findAll();
}
