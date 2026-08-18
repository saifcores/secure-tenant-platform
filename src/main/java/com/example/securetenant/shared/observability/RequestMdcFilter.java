package com.example.securetenant.shared.observability;

import com.example.securetenant.identity.domain.AuthenticatedUser;
import io.arconia.multitenancy.core.context.events.TenantContextAttachedEvent;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestMdcFilter extends OncePerRequestFilter implements Ordered {

    public static final String USER_ID = "userId";
    public static final String OPERATION = "operation";

    private final PlatformMetrics platformMetrics;

    public RequestMdcFilter(PlatformMetrics platformMetrics) {
        this.platformMetrics = platformMetrics;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        MDC.put(OPERATION, request.getMethod() + " " + request.getRequestURI());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser.from(authentication).ifPresent(user -> MDC.put(USER_ID, user.subject()));
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(USER_ID);
            MDC.remove(OPERATION);
        }
    }

    @EventListener
    public void onTenantAttached(TenantContextAttachedEvent event) {
        platformMetrics.incrementTenantRequests();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
