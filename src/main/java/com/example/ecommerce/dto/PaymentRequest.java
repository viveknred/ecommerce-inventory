package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotNull;

public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    public PaymentRequest() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}