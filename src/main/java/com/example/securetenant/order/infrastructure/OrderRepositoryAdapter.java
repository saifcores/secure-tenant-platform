package com.example.securetenant.order.infrastructure;

import com.example.securetenant.order.application.OrderRepository;
import com.example.securetenant.order.domain.Order;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository repository;

    public OrderRepositoryAdapter(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = repository.findById(order.id())
                .orElseGet(() -> OrderJpaEntity.fromDomain(order));
        entity.apply(order);
        return repository.save(entity).toDomain();
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return repository.findById(id).map(OrderJpaEntity::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return repository.findAll().stream().map(OrderJpaEntity::toDomain).toList();
    }

    @Override
    public long count() {
        return repository.count();
    }
}
