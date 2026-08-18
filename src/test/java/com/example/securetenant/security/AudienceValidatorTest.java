package com.example.securetenant.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AudienceValidatorTest {

    private final AudienceValidator validator = new AudienceValidator("securetenant-api");

    @Test
    void acceptsExpectedAudience() {
        Jwt jwt = jwt(List.of("securetenant-api", "account"));
        assertThat(validator.validate(jwt).hasErrors()).isFalse();
    }

    @Test
    void rejectsMissingAudience() {
        Jwt jwt = jwt(List.of("account"));
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertThat(result.hasErrors()).isTrue();
    }

    private Jwt jwt(List<String> audience) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .audience(audience)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("sub", "user-123")
                .build();
    }
}
