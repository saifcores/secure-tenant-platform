package com.example.securetenant.identity;

import com.example.securetenant.identity.infrastructure.KeycloakJwtGrantedAuthoritiesConverter;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakJwtGrantedAuthoritiesConverterTest {

        private final KeycloakJwtGrantedAuthoritiesConverter converter = new KeycloakJwtGrantedAuthoritiesConverter();

        @Test
        void mapsRealmAndCustomRoles() {
                Jwt jwt = Jwt.withTokenValue("token")
                                .header("alg", "none")
                                .issuedAt(Instant.now())
                                .expiresAt(Instant.now().plusSeconds(60))
                                .claim("sub", "user-123")
                                .claim("roles", List.of("TENANT_ADMIN"))
                                .claim("realm_access", Map.of("roles", List.of("USER", "offline_access")))
                                .build();

                List<String> authorities = converter.convert(jwt).stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList();

                assertThat(authorities).contains("ROLE_TENANT_ADMIN", "ROLE_USER");
                assertThat(authorities).doesNotContain("ROLE_OFFLINE_ACCESS");
        }
}
