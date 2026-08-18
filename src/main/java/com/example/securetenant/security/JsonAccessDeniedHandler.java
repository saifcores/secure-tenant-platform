package com.example.securetenant.security;

import com.example.securetenant.shared.api.ApiError;
import com.example.securetenant.shared.observability.PlatformMetrics;
import tools.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final JsonMapper jsonMapper;
    private final PlatformMetrics platformMetrics;

    public JsonAccessDeniedHandler(JsonMapper jsonMapper, PlatformMetrics platformMetrics) {
        this.jsonMapper = jsonMapper;
        this.platformMetrics = platformMetrics;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        platformMetrics.incrementSecurityDenied();
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError body = new ApiError(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                "Access denied",
                request.getRequestURI(),
                MDC.get("traceId"));
        jsonMapper.writeValue(response.getOutputStream(), body);
    }
}
