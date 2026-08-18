package com.example.securetenant.payment.infrastructure;

import com.example.securetenant.payment.application.ReconciliationService;
import com.example.securetenant.tenant.application.TenantRepository;
import io.arconia.multitenancy.core.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.payments.reconciliation.enabled", havingValue = "true")
public class ReconciliationJob {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationJob.class);

    private final TenantRepository tenantRepository;
    private final ReconciliationService reconciliationService;

    public ReconciliationJob(TenantRepository tenantRepository, ReconciliationService reconciliationService) {
        this.tenantRepository = tenantRepository;
        this.reconciliationService = reconciliationService;
    }

    @Scheduled(fixedDelayString = "${app.payments.reconciliation.interval:60s}")
    public void reconcile() {
        tenantRepository.findAll().forEach(tenant ->
                TenantContext.where(tenant.identifier()).run(() -> {
                    var report = reconciliationService.report();
                    if (!report.balanced()) {
                        log.warn(
                                "Reconciliation mismatch tenant={} unbalancedPayments={} settledWithoutSettlement={}",
                                tenant.identifier(),
                                report.unbalancedPaymentIds(),
                                report.settledWithoutSettlement()
                        );
                    }
                }));
    }
}
