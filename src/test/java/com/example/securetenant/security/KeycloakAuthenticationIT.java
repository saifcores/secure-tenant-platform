package com.example.securetenant.security;

import com.example.securetenant.PostgresTestConfiguration;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(PostgresTestConfiguration.class)
class KeycloakAuthenticationIT {

        @Container
        static final KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.3")
                        .withRealmImportFile("keycloak/securetenant-realm.json");

        @LocalServerPort
        int port;

        @DynamicPropertySource
        static void registerIssuer(DynamicPropertyRegistry registry) {
                registry.add(
                                "spring.security.oauth2.resourceserver.jwt.issuer-uri",
                                () -> keycloak.getAuthServerUrl() + "/realms/securetenant");
                registry.add("app.security.jwt.audience", () -> "securetenant-api");
        }

        @Test
        void aliceCanAuthenticateAndListOwnOrders() {
                String token = token("alice", "password");
                ResponseEntity<List<Map<String, Object>>> response = RestClient.create()
                                .get()
                                .uri("http://localhost:" + port + "/api/orders")
                                .header("Authorization", "Bearer " + token)
                                .retrieve()
                                .toEntity(new ParameterizedTypeReference<>() {
                                });
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody())
                                .isNotNull()
                                .allMatch(order -> "acme".equals(order.get("tenantId")));
        }

        @Test
        void expiredOrInvalidJwtIsDenied() {
                ResponseEntity<String> response = RestClient.create()
                                .get()
                                .uri("http://localhost:" + port + "/api/orders")
                                .header("Authorization", "Bearer not-a-jwt")
                                .retrieve()
                                .onStatus(status -> true, (req, res) -> {
                                })
                                .toEntity(String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        private String token(String username, String password) {
                var form = new LinkedMultiValueMap<String, String>();
                form.add("grant_type", "password");
                form.add("client_id", "securetenant-public");
                form.add("username", username);
                form.add("password", password);
                Map<?, ?> body = RestClient.create()
                                .post()
                                .uri(keycloak.getAuthServerUrl() + "/realms/securetenant/protocol/openid-connect/token")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .body(form)
                                .retrieve()
                                .body(Map.class);
                assertThat(body).isNotNull();
                return body.get("access_token").toString();
        }
}
