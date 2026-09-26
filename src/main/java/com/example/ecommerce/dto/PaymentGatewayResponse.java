package com.example.ecommerce.dto;

public record PaymentGatewayResponse(
        boolean success,
        String transactionId,
        String message) {
}