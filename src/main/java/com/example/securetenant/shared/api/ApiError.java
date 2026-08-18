package com.example.securetenant.shared.api;

import java.time.Instant;

public record ApiError(
                Instant timestamp,
                int status,
                String error,
                String message,
                String path,
                String traceId) {
}
