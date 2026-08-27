package com.example.ecommerce.dto;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

/**
 * Paginated reviews for one product plus the aggregate metrics required by
 * Phase 4 Module 3.
 */
public class ProductReviewsResponse {

    private Long productId;
    private String productName;
    private Double averageRating;
    private Long totalReviews;
    private Map<Integer, Long> ratingBreakdown;
    private List<ReviewResponse> reviews;
    private int page;
    private int size;
    private int totalPages;
    private boolean last;

    public ProductReviewsResponse() {
    }

    public ProductReviewsResponse(
            Long productId,
            String productName,
            Double averageRating,
            Long totalReviews,
            Map<Integer, Long> ratingBreakdown,
            Page<ReviewResponse> page) {

        this.productId = productId;
        this.productName = productName;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.ratingBreakdown = ratingBreakdown;
        this.reviews = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public Map<Integer, Long> getRatingBreakdown() {
        return ratingBreakdown;
    }

    public List<ReviewResponse> getReviews() {
        return reviews;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isLast() {
        return last;
    }
}
