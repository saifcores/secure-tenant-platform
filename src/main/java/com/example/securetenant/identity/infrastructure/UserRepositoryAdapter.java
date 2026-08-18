package com.example.securetenant.identity.infrastructure;

import com.example.securetenant.identity.application.UserRepository;
import com.example.securetenant.identity.domain.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository repository;

    public UserRepositoryAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(UserJpaEntity::toDomain);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream().map(UserJpaEntity::toDomain).toList();
    }
}
