package com.example.ecommerce.dto;

import java.time.LocalDateTime;

public class ReviewResponse {

    private Long id;
    private Long productId;
    private Long userId;
    private String userName;
    private Integer rating;
    private String comment;
    private String imageUrl;
    private LocalDateTime createdAt;

    public ReviewResponse() {
    }

    public ReviewResponse(
            Long id,
            Long productId,
            Long userId,
            String userName,
            Integer rating,
            String comment,
            String imageUrl,
            LocalDateTime createdAt) {

        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
