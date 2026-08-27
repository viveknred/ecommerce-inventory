package com.example.ecommerce.dto;

import java.math.BigDecimal;

import com.example.ecommerce.entity.Product;

public class ProductResponse {

    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private Integer stock;

    /**
     * Phase 4: marketplace attribution and review aggregates.
     */
    private Long vendorId;
    private String vendorName;
    private String imageUrl;
    private Double ratingAverage;
    private Integer reviewCount;

    public ProductResponse() {
    }

    public ProductResponse(
            Long id,
            String name,
            String category,
            BigDecimal price,
            Integer stock,
            Long vendorId,
            String vendorName,
            String imageUrl,
            Double ratingAverage,
            Integer reviewCount) {

        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.imageUrl = imageUrl;
        this.ratingAverage = ratingAverage;
        this.reviewCount = reviewCount;
    }

    /**
     * Single mapping point from entity to response, so every endpoint that
     * returns a product exposes exactly the same shape.
     */
    public static ProductResponse from(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStock(),
                product.getVendor() != null ? product.getVendor().getId() : null,
                product.getVendor() != null ? product.getVendor().getBusinessName() : null,
                product.getImageUrl(),
                product.getRatingAverage(),
                product.getReviewCount()
        );
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

    public Long getVendorId() {
        return vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Double getRatingAverage() {
        return ratingAverage;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }
}
