package com.example.ecommerce.event;

public record StockLowWebhookEvent(
        Long productId) {
}