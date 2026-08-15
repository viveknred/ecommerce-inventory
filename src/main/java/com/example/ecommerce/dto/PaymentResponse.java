package com.example.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.ecommerce.entity.PaymentStatus;

public class PaymentResponse {

    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime createdAt;

    public PaymentResponse(
            Long id,
            Long orderId,
            BigDecimal amount,
            PaymentStatus status,
            String transactionId,
            LocalDateTime createdAt) {

        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.transactionId = transactionId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}