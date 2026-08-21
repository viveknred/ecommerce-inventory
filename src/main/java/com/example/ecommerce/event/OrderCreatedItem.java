package com.example.ecommerce.event;

import java.math.BigDecimal;

public class OrderCreatedItem {

    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;

    public OrderCreatedItem() {
    }

    public OrderCreatedItem(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice) {

        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}