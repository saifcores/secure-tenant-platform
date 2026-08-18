package com.example.securetenant.identity.api;

import com.example.securetenant.identity.application.UserQueryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserQueryService userQueryService;

    public UserController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','AUDITOR')")
    public List<UserApi.UserResponse> list() {
        return userQueryService.list().stream().map(UserApi.UserResponse::from).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','AUDITOR')")
    public UserApi.UserResponse get(@PathVariable UUID id) {
        return UserApi.UserResponse.from(userQueryService.get(id));
    }
}
