package com.example.securetenant.security;

import com.example.securetenant.shared.api.ApiError;
import com.example.securetenant.shared.observability.PlatformMetrics;
import tools.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JsonMapper jsonMapper;
    private final PlatformMetrics platformMetrics;

    public JsonAuthenticationEntryPoint(JsonMapper jsonMapper, PlatformMetrics platformMetrics) {
        this.jsonMapper = jsonMapper;
        this.platformMetrics = platformMetrics;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        platformMetrics.incrementSecurityDenied();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError body = new ApiError(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                "Authentication required",
                request.getRequestURI(),
                MDC.get("traceId"));
        jsonMapper.writeValue(response.getOutputStream(), body);
    }
}
