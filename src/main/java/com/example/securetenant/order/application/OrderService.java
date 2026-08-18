package com.example.securetenant.order.application;

import com.example.securetenant.audit.application.AuditRecorder;
import com.example.securetenant.customer.application.CustomerRepository;
import com.example.securetenant.order.domain.Order;
import com.example.securetenant.order.domain.OrderStateMachine;
import com.example.securetenant.order.domain.OrderStatus;
import com.example.securetenant.payment.application.PaymentRepository;
import com.example.securetenant.security.CurrentTenant;
import com.example.securetenant.shared.api.BusinessRuleException;
import com.example.securetenant.shared.api.ResourceNotFoundException;
import com.example.securetenant.shared.observability.PlatformMetrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final AuditRecorder auditRecorder;
    private final PlatformMetrics platformMetrics;

    public OrderService(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            PaymentRepository paymentRepository,
            AuditRecorder auditRecorder,
            PlatformMetrics platformMetrics) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.auditRecorder = auditRecorder;
        this.platformMetrics = platformMetrics;
    }

    @Transactional(readOnly = true)
    public List<Order> list() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Order get(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Transactional
    public Order create(CreateOrderCommand command) {
        try {
            customerRepository.findById(command.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            Order order = new Order(
                    UUID.randomUUID(),
                    CurrentTenant.require(),
                    command.customerId(),
                    command.amount(),
                    command.currency().toUpperCase(Locale.ROOT),
                    OrderStatus.CREATED,
                    null,
                    null);
            Order saved = orderRepository.save(order);
            auditRecorder.record("ORDER_CREATED", "ORDER", saved.id().toString(), saved.tenantId());
            platformMetrics.incrementOrdersCreated();
            return saved;
        } catch (RuntimeException ex) {
            platformMetrics.incrementOrdersFailed();
            throw ex;
        }
    }

    @Transactional
    public Order confirm(UUID id) {
        return transition(id, OrderStatus.CONFIRMED, "ORDER_CONFIRMED");
    }

    @Transactional
    public Order cancel(UUID id) {
        if (paymentRepository.existsOpenForOrder(id)) {
            throw new BusinessRuleException("Cannot cancel an order with an in-flight or settled payment");
        }
        return transition(id, OrderStatus.CANCELLED, "ORDER_CANCELLED");
    }

    @Transactional
    public Order complete(UUID id) {
        return transition(id, OrderStatus.COMPLETED, "ORDER_COMPLETED");
    }

    @Transactional
    public Order completeAfterSettlement(UUID id) {
        Order existing = get(id);
        if (existing.status() == OrderStatus.COMPLETED) {
            return existing;
        }
        return complete(id);
    }

    private Order transition(UUID id, OrderStatus target, String action) {
        Order existing = get(id);
        OrderStatus next = OrderStateMachine.transition(existing.status(), target);
        Order updated = new Order(
                existing.id(),
                existing.tenantId(),
                existing.customerId(),
                existing.amount(),
                existing.currency(),
                next,
                existing.createdAt(),
                existing.updatedAt());
        Order saved = orderRepository.save(updated);
        auditRecorder.record(action, "ORDER", saved.id().toString(), saved.tenantId());
        return saved;
    }
}
