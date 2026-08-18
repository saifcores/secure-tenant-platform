package com.example.securetenant.order.api;

import com.example.securetenant.order.application.CreateOrderCommand;
import com.example.securetenant.order.application.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Tenant-scoped order lifecycle")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER','USER','AUDITOR')")
    public List<OrderApi.OrderResponse> list() {
        return orderService.list().stream().map(OrderApi.OrderResponse::from).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER','USER','AUDITOR')")
    public OrderApi.OrderResponse get(@PathVariable UUID id) {
        return OrderApi.OrderResponse.from(orderService.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER','USER')")
    public OrderApi.OrderResponse create(@Valid @RequestBody OrderApi.CreateOrderRequest request) {
        return OrderApi.OrderResponse.from(orderService.create(
                new CreateOrderCommand(request.customerId(), request.amount(), request.currency())));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER')")
    public OrderApi.OrderResponse confirm(@PathVariable UUID id) {
        return OrderApi.OrderResponse.from(orderService.confirm(id));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER')")
    public OrderApi.OrderResponse cancel(@PathVariable UUID id) {
        return OrderApi.OrderResponse.from(orderService.cancel(id));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','MANAGER')")
    public OrderApi.OrderResponse complete(@PathVariable UUID id) {
        return OrderApi.OrderResponse.from(orderService.complete(id));
    }
}
