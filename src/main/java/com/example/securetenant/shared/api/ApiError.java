package com.example.securetenant.shared.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standard error envelope. No stack traces or SQL details.")
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String traceId) {
}
