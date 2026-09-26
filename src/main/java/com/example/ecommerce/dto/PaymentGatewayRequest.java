package com.example.ecommerce.dto;

import java.math.BigDecimal;

public record PaymentGatewayRequest(
        Long orderId,
        BigDecimal amount) {
}