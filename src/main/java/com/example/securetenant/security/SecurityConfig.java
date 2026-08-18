package com.example.securetenant.security;

import com.example.securetenant.identity.infrastructure.KeycloakJwtGrantedAuthoritiesConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        private final JsonAuthenticationEntryPoint authenticationEntryPoint;
        private final JsonAccessDeniedHandler accessDeniedHandler;
        private final KeycloakJwtGrantedAuthoritiesConverter authoritiesConverter;

        public SecurityConfig(
                        JsonAuthenticationEntryPoint authenticationEntryPoint,
                        JsonAccessDeniedHandler accessDeniedHandler,
                        KeycloakJwtGrantedAuthoritiesConverter authoritiesConverter) {
                this.authenticationEntryPoint = authenticationEntryPoint;
                this.accessDeniedHandler = accessDeniedHandler;
                this.authoritiesConverter = authoritiesConverter;
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(Customizer.withDefaults())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/actuator/health",
                                                                "/actuator/health/**",
                                                                "/actuator/info",
                                                                "/actuator/prometheus",
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs",
                                                                "/v3/api-docs/**")
                                                .permitAll()
                                                .requestMatchers("/api/tenants/**").hasRole("PLATFORM_ADMIN")
                                                .requestMatchers("/api/platform/**").hasRole("PLATFORM_ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/customers/**")
                                                .hasRole("TENANT_ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/customers/**")
                                                .hasAnyRole("TENANT_ADMIN", "MANAGER")
                                                .requestMatchers(HttpMethod.PUT, "/api/customers/**")
                                                .hasAnyRole("TENANT_ADMIN", "MANAGER")
                                                .requestMatchers(HttpMethod.GET, "/api/customers/**")
                                                .hasAnyRole("TENANT_ADMIN", "MANAGER", "USER", "AUDITOR")
                                                .requestMatchers(HttpMethod.POST, "/api/orders")
                                                .hasAnyRole("TENANT_ADMIN", "MANAGER", "USER")
                                                .requestMatchers(HttpMethod.PUT, "/api/orders/**")
                                                .hasAnyRole("TENANT_ADMIN", "MANAGER")
                                                .requestMatchers(HttpMethod.GET, "/api/orders/**")
                                                .hasAnyRole("TENANT_ADMIN", "MANAGER", "USER", "AUDITOR")
                                                .requestMatchers(HttpMethod.POST, "/api/payments/{id}/retry")
                                                .hasAnyRole("TENANT_ADMIN", "MANAGER")
                                                .requestMatchers(HttpMethod.POST, "/api/payments/{id}/cancel")
                                                .hasAnyRole("TENANT_ADMIN", "MANAGER")
                                                .requestMatchers(HttpMethod.POST, "/api/payments")
                                                .hasAnyRole("TENANT_ADMIN", "MANAGER", "USER")
                                                .requestMatchers(HttpMethod.GET, "/api/users/**")
                                                .hasAnyRole("TENANT_ADMIN", "AUDITOR")
                                                .requestMatchers(HttpMethod.GET, "/api/payments/**")
                                                .hasAnyRole("TENANT_ADMIN", "MANAGER", "USER", "AUDITOR")
                                                .requestMatchers(HttpMethod.GET, "/api/wallets/**")
                                                .hasAnyRole("TENANT_ADMIN", "MANAGER", "USER", "AUDITOR")
                                                .requestMatchers(HttpMethod.GET, "/api/settlements/**")
                                                .hasAnyRole("TENANT_ADMIN", "MANAGER", "USER", "AUDITOR")
                                                .requestMatchers(HttpMethod.GET, "/api/reconciliation")
                                                .hasAnyRole("TENANT_ADMIN", "MANAGER", "AUDITOR")
                                                .requestMatchers("/api/audit/**")
                                                .hasAnyRole("PLATFORM_ADMIN", "TENANT_ADMIN", "AUDITOR")
                                                .requestMatchers("/api/stats/**")
                                                .hasAnyRole("TENANT_ADMIN", "MANAGER")
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .authenticationEntryPoint(authenticationEntryPoint)
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                                jwtAuthenticationConverter())))
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(authenticationEntryPoint)
                                                .accessDeniedHandler(accessDeniedHandler));
                return http.build();
        }

        @Bean
        JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
                return converter;
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource(
                        @Value("${app.security.cors.allowed-origins}") String allowedOrigins) {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key"));
                configuration.setAllowCredentials(false);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
