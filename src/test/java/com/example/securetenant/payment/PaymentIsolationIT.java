package com.example.securetenant.payment;

import com.example.securetenant.PostgresTestConfiguration;
import com.example.securetenant.TestJwtDecoderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import({ PostgresTestConfiguration.class, TestJwtDecoderConfig.class })
class PaymentIsolationIT {

    private static final UUID ACME_CREATED_ORDER = UUID.fromString("cccccccc-cccc-cccc-cccc-ccccccccccc1");
    private static final UUID ACME_CONFIRMED_ORDER = UUID.fromString("cccccccc-cccc-cccc-cccc-ccccccccccc2");
    private static final UUID GLOBEX_ORDER = UUID.fromString("dddddddd-dddd-dddd-dddd-ddddddddddd1");
    private static final UUID ACME_CUSTOMER = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonMapper jsonMapper;

    @Test
    void alicePaysOwnConfirmedOrderAndCannotSeeGlobexPayments() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", "alice-pay-acme-confirmed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "orderId": "%s" }
                                """.formatted(ACME_CONFIRMED_ORDER))
                        .with(alice()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value("acme"))
                .andExpect(jsonPath("$.status").value("SETTLED"))
                .andReturn();

        String paymentId = jsonMapper.readTree(created.getResponse().getContentAsString())
                .get("id")
                .asString();

        mockMvc.perform(get("/api/payments/{id}", paymentId).with(john()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/payments").with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tenantId", not(hasItem("globex"))));

        mockMvc.perform(get("/api/orders/{id}", ACME_CONFIRMED_ORDER).with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void unconfirmedOrderCannotBePaid() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", "alice-pay-unconfirmed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "orderId": "%s" }
                                """.formatted(ACME_CREATED_ORDER))
                        .with(alice()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void aliceCannotPayGlobexOrder() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", "alice-pay-globex")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "orderId": "%s" }
                                """.formatted(GLOBEX_ORDER))
                        .with(alice()))
                .andExpect(status().isNotFound());
    }

    @Test
    void sameIdempotencyKeyReplaysTheSamePayment() throws Exception {
        String orderId = createConfirmedOrder("31.00");
        String body = """
                { "orderId": "%s" }
                """.formatted(orderId);
        MvcResult first = mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", "alice-idempotent-replay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(alice()))
                .andExpect(status().isCreated())
                .andReturn();
        String firstId = jsonMapper.readTree(first.getResponse().getContentAsString())
                .get("id")
                .asString();

        mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", "alice-idempotent-replay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(alice()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(firstId));
    }

    @Test
    void sameIdempotencyKeyWithDifferentBodyConflicts() throws Exception {
        String firstOrderId = createConfirmedOrder("32.00");
        String secondOrderId = createConfirmedOrder("33.00");

        mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", "alice-idempotent-conflict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "orderId": "%s" }
                                """.formatted(firstOrderId))
                        .with(alice()))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", "alice-idempotent-conflict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "orderId": "%s" }
                                """.formatted(secondOrderId))
                        .with(alice()))
                .andExpect(status().isConflict());
    }

    @Test
    void walletListIsTenantScopedAndReconciliationBalances() throws Exception {
        mockMvc.perform(get("/api/wallets")
                        .header("X-Tenant-ID", "globex")
                        .with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tenantId", not(hasItem("globex"))))
                .andExpect(jsonPath("$[*].tenantId", hasItem("acme")));

        mockMvc.perform(get("/api/reconciliation").with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanced").value(true));
    }

    @Test
    void hardDeclineFailsPayment() throws Exception {
        String orderId = createConfirmedOrder("10.07");

        mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", "alice-hard-decline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "orderId": "%s" }
                                """.formatted(orderId))
                        .with(alice()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    void retryablePaymentCanBeCancelled() throws Exception {
        String orderId = createConfirmedOrder("10.99");

        MvcResult created = mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", "alice-cancel-retryable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "orderId": "%s" }
                                """.formatted(orderId))
                        .with(alice()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andReturn();
        String paymentId = jsonMapper.readTree(created.getResponse().getContentAsString())
                .get("id")
                .asString();

        mockMvc.perform(post("/api/payments/{id}/cancel", paymentId).with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cannotPayTheSameOrderTwice() throws Exception {
        String orderId = createConfirmedOrder("34.00");
        mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", "alice-dup-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "orderId": "%s" }
                                """.formatted(orderId))
                        .with(alice()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SETTLED"));

        mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", "alice-dup-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "orderId": "%s" }
                                """.formatted(orderId))
                        .with(alice()))
                .andExpect(status().isUnprocessableEntity());
    }

    private String createConfirmedOrder(String amount) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s",
                                  "amount": %s,
                                  "currency": "USD"
                                }
                                """.formatted(ACME_CUSTOMER, amount))
                        .with(alice()))
                .andExpect(status().isCreated())
                .andReturn();
        String orderId = jsonMapper.readTree(created.getResponse().getContentAsString())
                .get("id")
                .asString();
        mockMvc.perform(put("/api/orders/{id}/confirm", orderId).with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
        return orderId;
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor alice() {
        return jwt().jwt(jwt -> jwt
                        .subject("alice")
                        .claim("preferred_username", "alice")
                        .claim("tenant_id", "acme")
                        .claim("roles", java.util.List.of("TENANT_ADMIN")))
                .authorities(new SimpleGrantedAuthority("ROLE_TENANT_ADMIN"));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor john() {
        return jwt().jwt(jwt -> jwt
                        .subject("john")
                        .claim("preferred_username", "john")
                        .claim("tenant_id", "globex")
                        .claim("roles", java.util.List.of("TENANT_ADMIN")))
                .authorities(new SimpleGrantedAuthority("ROLE_TENANT_ADMIN"));
    }
}
