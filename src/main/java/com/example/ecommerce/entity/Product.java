package com.example.ecommerce.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_product_category", columnList = "category"),
        @Index(name = "idx_products_vendor", columnList = "vendor_id")
    }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String category;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @NotNull
    @Min(0)
    @Column(name = "stock_quantity", nullable = false)
    private Integer stock;

    /**
     * Phase 4: owning vendor in the marketplace.
     * Nullable so pre-Phase-4 catalogue rows remain valid.
     */
    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    /**
     * Phase 4: public URL of the product image, produced by the media endpoint.
     */
    @Column(name = "image_url")
    private String imageUrl;

    /**
     * Phase 4: denormalised rating, recalculated whenever a review is added.
     */
    @Column(name = "rating_average", nullable = false)
    private Double ratingAverage;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount;

    @Version
    @Column(nullable = false)
    private Long version;

    public Product() {
    }

    @PrePersist
    public void prePersist() {

        if (ratingAverage == null) {
            ratingAverage = 0.0;
        }

        if (reviewCount == null) {
            reviewCount = 0;
        }
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

    public Vendor getVendor() {
        return vendor;
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

    public Long getVersion() {
        return version;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setRatingAverage(Double ratingAverage) {
        this.ratingAverage = ratingAverage;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }
}
