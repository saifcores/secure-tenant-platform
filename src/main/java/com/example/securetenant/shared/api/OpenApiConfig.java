package com.example.securetenant.shared.api;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        public static final String BEARER_JWT = "bearer-jwt";
        public static final String KEYCLOAK = "keycloak";

        @Bean
        OpenAPI secureTenantOpenApi(
                        @Value("${app.openapi.authorization-url}") String authorizationUrl,
                        @Value("${app.openapi.token-url}") String tokenUrl) {
                Scopes openId = new Scopes().addString("openid", "OpenID Connect");
                return new OpenAPI()
                                .info(new Info()
                                                .title("SecureTenant Platform API")
                                                .version("1.0.0")
                                                .description("""
                                                                Multi-tenant B2B SaaS API. Tenant is taken only from the \
                                                                validated JWT claim `tenant_id`. `X-Tenant-ID` is ignored. \
                                                                Cross-tenant ids return 404.
                                                                """))
                                .addSecurityItem(new SecurityRequirement().addList(BEARER_JWT))
                                .addSecurityItem(new SecurityRequirement().addList(KEYCLOAK))
                                .components(new Components()
                                                .addSecuritySchemes(BEARER_JWT, new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .description("Paste a Keycloak access token."))
                                                .addSecuritySchemes(KEYCLOAK, new SecurityScheme()
                                                                .type(SecurityScheme.Type.OAUTH2)
                                                                .description("Keycloak public client `securetenant-public`.")
                                                                .flows(new OAuthFlows()
                                                                                .password(new OAuthFlow()
                                                                                                .tokenUrl(tokenUrl)
                                                                                                .scopes(openId))
                                                                                .authorizationCode(new OAuthFlow()
                                                                                                .authorizationUrl(
                                                                                                                authorizationUrl)
                                                                                                .tokenUrl(tokenUrl)
                                                                                                .scopes(openId)))));
        }
}
