package com.example.ecommerce.dto;

import java.time.LocalDateTime;

public class CouponResponse {

    private Long id;
    private String code;
    private Integer discountPercent;
    private LocalDateTime expirationDate;
    private Boolean isActive;

    public CouponResponse() {
    }

    public CouponResponse(
            Long id,
            String code,
            Integer discountPercent,
            LocalDateTime expirationDate,
            Boolean isActive) {

        this.id = id;
        this.code = code;
        this.discountPercent = discountPercent;
        this.expirationDate = expirationDate;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Integer getDiscountPercent() {
        return discountPercent;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }
}