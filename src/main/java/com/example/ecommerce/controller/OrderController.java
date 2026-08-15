package com.example.ecommerce.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.dto.OrderRequest;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Order create(
            @Valid @RequestBody OrderRequest request,
            Authentication authentication) {

        return orderService.createForAuthenticatedUser(
                request,
                authentication.getName()
        );
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public List<Order> getMyOrders(
            Authentication authentication) {

        return orderService.getMyOrders(
                authentication.getName()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public Order getById(
            @PathVariable("id") Long id,
            Authentication authentication) {

        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN")
                        );

        if (isAdmin) {
            return orderService.getById(id);
        }

        return orderService.getByIdForUser(
                id,
                authentication.getName()
        );
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Order> getByUserId(
            @PathVariable("userId") Long userId) {

        return orderService.getByUserId(userId);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Order updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("newStatus")
            OrderStatus newStatus) {

        return orderService.updateStatus(
                id,
                newStatus
        );
    }
}