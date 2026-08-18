package com.example.securetenant.identity.infrastructure;

import com.example.securetenant.identity.domain.UserRole;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        addRoles(authorities, jwt.getClaimAsStringList("roles"));
        addRealmRoles(authorities, jwt.getClaimAsMap("realm_access"));
        return authorities;
    }

    private void addRealmRoles(List<GrantedAuthority> authorities, Map<String, Object> realmAccess) {
        if (realmAccess == null) {
            return;
        }
        Object roles = realmAccess.get("roles");
        if (roles instanceof Collection<?> collection) {
            addRoles(authorities, collection.stream().map(Object::toString).toList());
        }
    }

    private void addRoles(List<GrantedAuthority> authorities, List<String> roles) {
        if (roles == null) {
            return;
        }
        for (String role : roles) {
            String normalized = role.trim().toUpperCase(Locale.ROOT);
            if (normalized.startsWith("ROLE_")) {
                normalized = normalized.substring(5);
            }
            try {
                UserRole.valueOf(normalized);
                authorities.add(new SimpleGrantedAuthority("ROLE_" + normalized));
            } catch (IllegalArgumentException ignored) {
                // ignore non-application roles such as default Keycloak roles
            }
        }
    }
}
