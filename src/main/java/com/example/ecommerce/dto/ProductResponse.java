package com.example.ecommerce.dto;

import java.math.BigDecimal;

public class ProductResponse {

    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private Integer stock;

    public ProductResponse(
            Long id,
            String name,
            String category,
            BigDecimal price,
            Integer stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }
}