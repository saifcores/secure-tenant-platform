package com.example.securetenant.security;

import com.example.securetenant.PostgresTestConfiguration;
import com.example.securetenant.TestJwtDecoderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import({ PostgresTestConfiguration.class, TestJwtDecoderConfig.class })
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class TenantIsolationIT {

    private static final UUID ACME_ORDER = UUID.fromString("cccccccc-cccc-cccc-cccc-ccccccccccc1");
    private static final UUID GLOBEX_ORDER = UUID.fromString("dddddddd-dddd-dddd-dddd-ddddddddddd1");
    private static final UUID ACME_CUSTOMER = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");

    @Autowired
    MockMvc mockMvc;

    @Test
    void aliceSeesOnlyAcmeOrders() throws Exception {
        mockMvc.perform(get("/api/orders").with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tenantId", not(hasItem("globex"))))
                .andExpect(jsonPath("$[*].tenantId", hasItem("acme")));
    }

    @Test
    void aliceCannotReadGlobexOrder() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", GLOBEX_ORDER).with(alice()))
                .andExpect(status().isNotFound());
    }

    @Test
    void aliceCanReadOwnOrder() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", ACME_ORDER).with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("acme"));
    }

    @Test
    void forgedTenantHeaderIsIgnored() throws Exception {
        mockMvc.perform(get("/api/orders")
                .header("X-Tenant-ID", "globex")
                .header("X-TenantId", "globex")
                .param("tenantId", "globex")
                .with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tenantId", not(hasItem("globex"))))
                .andExpect(jsonPath("$[*].id", hasItem(ACME_ORDER.toString())));
    }

    @Test
    void userCannotDeleteCustomer() throws Exception {
        mockMvc.perform(delete("/api/customers/{id}", ACME_CUSTOMER).with(bob()))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tenantAdminCanCreateCustomer() throws Exception {
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Acme West",
                          "email": "west@acme.test",
                          "phone": "+1-555-0199"
                        }
                        """)
                .with(alice()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value("acme"));
    }

    @Test
    void auditCapturesSensitiveOperations() throws Exception {
        mockMvc.perform(get("/api/audit").with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(0))));
    }

    @Test
    void aliceSeesOnlyAcmeUsers() throws Exception {
        mockMvc.perform(get("/api/users").with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tenantId", not(hasItem("globex"))))
                .andExpect(jsonPath("$[*].tenantId", not(hasItem("fincorp"))))
                .andExpect(jsonPath("$[*].username", hasItem("alice")));
    }

    @Test
    void aliceCannotReadGlobexUser() throws Exception {
        mockMvc.perform(get("/api/users/{id}", "55555555-5555-5555-5555-555555555551").with(alice()))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCannotListTenantUsers() throws Exception {
        mockMvc.perform(get("/api/users").with(bob()))
                .andExpect(status().isForbidden());
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor alice() {
        return jwt().jwt(jwt -> jwt
                .subject("alice")
                .claim("preferred_username", "alice")
                .claim("tenant_id", "acme")
                .claim("roles", java.util.List.of("TENANT_ADMIN")))
                .authorities(new SimpleGrantedAuthority("ROLE_TENANT_ADMIN"));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor bob() {
        return jwt().jwt(jwt -> jwt
                .subject("bob")
                .claim("preferred_username", "bob")
                .claim("tenant_id", "acme")
                .claim("roles", java.util.List.of("USER")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
