package com.example.securetenant.payment.api;

import com.example.securetenant.idempotency.application.IdempotentPaymentExecutor;
import com.example.securetenant.ledger.application.LedgerService;
import com.example.securetenant.payment.application.CreatePaymentCommand;
import com.example.securetenant.payment.application.PaymentOrchestrator;
import com.example.securetenant.payment.application.ReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Payments", description = "Wallet reservation, PSP, settlement")
public class PaymentController {

    private final PaymentOrchestrator paymentOrchestrator;
    private final IdempotentPaymentExecutor idempotentPaymentExecutor;
    private final LedgerService ledgerService;
    private final ReconciliationService reconciliationService;

    public PaymentController(
            PaymentOrchestrator paymentOrchestrator,
            IdempotentPaymentExecutor idempotentPaymentExecutor,
            LedgerService ledgerService,
            ReconciliationService reconciliationService) {
        this.paymentOrchestrator = paymentOrchestrator;
        this.idempotentPaymentExecutor = idempotentPaymentExecutor;
        this.ledgerService = ledgerService;
        this.reconciliationService = reconciliationService;
    }

    @GetMapping("/payments")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER','USER','AUDITOR')")
    public List<PaymentApi.PaymentResponse> list() {
        return paymentOrchestrator.list().stream().map(PaymentApi.PaymentResponse::from).toList();
    }

    @GetMapping("/payments/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER','USER','AUDITOR')")
    public PaymentApi.PaymentResponse get(@PathVariable UUID id) {
        return PaymentApi.PaymentResponse.from(paymentOrchestrator.get(id));
    }

    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER','USER')")
    @Operation(summary = "Create a payment for a CONFIRMED order")
    public PaymentApi.PaymentResponse create(
            @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER, required = true, description = "Unique per tenant. Same key + same orderId replays; different orderId returns 409.") @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentApi.CreatePaymentRequest request) {
        CreatePaymentCommand command = new CreatePaymentCommand(request.orderId(), idempotencyKey);
        return PaymentApi.PaymentResponse.from(
                idempotentPaymentExecutor.execute(command, () -> paymentOrchestrator.initiate(command)));
    }

    @PostMapping("/payments/{id}/retry")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER')")
    public PaymentApi.PaymentResponse retry(@PathVariable UUID id) {
        return PaymentApi.PaymentResponse.from(paymentOrchestrator.retry(id));
    }

    @PostMapping("/payments/{id}/cancel")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER')")
    public PaymentApi.PaymentResponse cancel(@PathVariable UUID id) {
        return PaymentApi.PaymentResponse.from(paymentOrchestrator.cancel(id));
    }

    @GetMapping("/payments/{id}/transactions")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER','USER','AUDITOR')")
    public List<PaymentApi.PaymentTransactionResponse> transactions(@PathVariable UUID id) {
        return paymentOrchestrator.transactions(id).stream()
                .map(PaymentApi.PaymentTransactionResponse::from)
                .toList();
    }

    @GetMapping("/payments/{id}/ledger")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER','USER','AUDITOR')")
    public List<PaymentApi.LedgerEntryResponse> ledger(@PathVariable UUID id) {
        paymentOrchestrator.get(id);
        return ledgerService.findByPayment(id).stream()
                .map(PaymentApi.LedgerEntryResponse::from)
                .toList();
    }

    @GetMapping("/settlements")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER','USER','AUDITOR')")
    @Operation(tags = { "Settlements" })
    public List<PaymentApi.SettlementResponse> settlements() {
        return paymentOrchestrator.settlements().stream()
                .map(PaymentApi.SettlementResponse::from)
                .toList();
    }

    @GetMapping("/reconciliation")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER','AUDITOR')")
    @Operation(tags = { "Reconciliation" })
    public PaymentApi.ReconciliationResponse reconciliation() {
        return PaymentApi.ReconciliationResponse.from(reconciliationService.report());
    }
}
