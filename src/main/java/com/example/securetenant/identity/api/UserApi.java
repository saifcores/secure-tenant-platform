package com.example.securetenant.identity.api;

import com.example.securetenant.identity.domain.User;
import com.example.securetenant.identity.domain.UserRole;

import java.time.Instant;
import java.util.UUID;

public final class UserApi {

    private UserApi() {
    }

    public record UserResponse(
            UUID id,
            String tenantId,
            String username,
            String email,
            UserRole role,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static UserResponse from(User user) {
            return new UserResponse(
                    user.id(),
                    user.tenantId(),
                    user.username(),
                    user.email(),
                    user.role(),
                    user.createdAt(),
                    user.updatedAt()
            );
        }
    }
}
