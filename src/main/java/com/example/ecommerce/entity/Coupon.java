package com.example.ecommerce.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String code;

    @NotNull
    @Min(1)
    @Max(100)
    @Column(name = "discount_percent", nullable = false)
    private Integer discountPercent;

    @NotNull
    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @PrePersist
    public void prePersist() {
        if (isActive == null) {
            isActive = true;
        }
    }

    public Coupon() {
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

    public void setCode(String code) {
        this.code = code;
    }

    public void setDiscountPercent(Integer discountPercent) {
        this.discountPercent = discountPercent;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}