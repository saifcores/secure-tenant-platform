package com.example.securetenant.identity.application;

import com.example.securetenant.identity.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findById(UUID id);

    List<User> findAll();
}
