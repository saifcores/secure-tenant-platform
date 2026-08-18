package com.example.securetenant.identity.domain;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public record AuthenticatedUser(
        String subject,
        String username,
        Optional<String> tenantId,
        Set<UserRole> roles) {

    public static Optional<AuthenticatedUser> from(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.of(fromJwt(jwtAuth));
        }
        return Optional.empty();
    }

    public static AuthenticatedUser fromJwt(JwtAuthenticationToken authentication) {
        Jwt jwt = authentication.getToken();
        Set<UserRole> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(UserRole::fromAuthority)
                .collect(Collectors.toUnmodifiableSet());
        String username = Optional.ofNullable(jwt.getClaimAsString("preferred_username"))
                .orElse(jwt.getSubject());
        Optional<String> tenantId = Optional.ofNullable(jwt.getClaimAsString("tenant_id"))
                .filter(value -> !value.isBlank());
        return new AuthenticatedUser(jwt.getSubject(), username, tenantId, roles);
    }

    public boolean hasRole(UserRole role) {
        return roles.contains(role);
    }

    public boolean isPlatformAdmin() {
        return hasRole(UserRole.PLATFORM_ADMIN);
    }
}
