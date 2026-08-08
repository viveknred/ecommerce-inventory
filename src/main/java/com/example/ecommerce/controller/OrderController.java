package com.example.ecommerce.controller;

import java.util.List;

import com.example.ecommerce.dto.OrderRequest;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order create(@Valid @RequestBody OrderRequest request) {
        return orderService.create(request);
    }

    @GetMapping("/{id}")
    public Order getById(@PathVariable("id") Long id) {
        return orderService.getById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Order> getByUserId(@PathVariable("userId") Long userId) {
        return orderService.getByUserId(userId);
    }

    @PatchMapping("/{id}/status")
    public Order updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") OrderStatus status) {

        return orderService.updateStatus(id, status);
    }
}