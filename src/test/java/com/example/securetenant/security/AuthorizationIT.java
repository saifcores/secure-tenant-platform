package com.example.securetenant.security;

import com.example.securetenant.PostgresTestConfiguration;
import com.example.securetenant.TestJwtDecoderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import({ PostgresTestConfiguration.class, TestJwtDecoderConfig.class })
class AuthorizationIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void userCannotAccessAdminApi() throws Exception {
        mockMvc.perform(post("/api/tenants")
                .contentType("application/json")
                .content("""
                        {"identifier":"evil","name":"Evil Corp"}
                        """)
                .with(jwt().jwt(jwt -> jwt
                        .subject("bob")
                        .claim("tenant_id", "acme")
                        .claim("roles", java.util.List.of("USER")))
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCannotListTenantUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                .with(jwt().jwt(jwt -> jwt
                        .subject("bob")
                        .claim("tenant_id", "acme")
                        .claim("roles", java.util.List.of("USER")))
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void platformAdminCanListTenants() throws Exception {
        mockMvc.perform(get("/api/tenants")
                .with(jwt().jwt(jwt -> jwt
                        .subject("platform-admin")
                        .claim("roles", java.util.List.of("PLATFORM_ADMIN")))
                        .authorities(new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void unknownTenantIsRejected() throws Exception {
        mockMvc.perform(get("/api/orders")
                .with(jwt().jwt(jwt -> jwt
                        .subject("ghost")
                        .claim("tenant_id", "unknown")
                        .claim("roles", java.util.List.of("TENANT_ADMIN")))
                        .authorities(new SimpleGrantedAuthority("ROLE_TENANT_ADMIN"))))
                .andExpect(status().is4xxClientError());
    }
}
