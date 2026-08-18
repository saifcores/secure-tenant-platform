package com.example.securetenant.order.api;

import com.example.securetenant.customer.application.CustomerRepository;
import com.example.securetenant.order.application.OrderRepository;
import com.example.securetenant.security.CurrentTenant;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public StatsController(OrderRepository orderRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER')")
    public OrderApi.TenantStatsResponse stats() {
        return new OrderApi.TenantStatsResponse(
                CurrentTenant.require(),
                orderRepository.count(),
                customerRepository.findAll().size());
    }
}
