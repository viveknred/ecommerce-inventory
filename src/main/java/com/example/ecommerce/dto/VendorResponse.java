package com.example.ecommerce.dto;

import java.time.LocalDateTime;

public class VendorResponse {

    private Long id;
    private String businessName;
    private String supportEmail;
    private String logoUrl;
    private Double ratingAverage;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private String ownerEmail;

    public VendorResponse() {
    }

    public VendorResponse(
            Long id,
            String businessName,
            String supportEmail,
            String logoUrl,
            Double ratingAverage,
            Boolean isVerified,
            LocalDateTime createdAt,
            String ownerEmail) {

        this.id = id;
        this.businessName = businessName;
        this.supportEmail = supportEmail;
        this.logoUrl = logoUrl;
        this.ratingAverage = ratingAverage;
        this.isVerified = isVerified;
        this.createdAt = createdAt;
        this.ownerEmail = ownerEmail;
    }

    public Long getId() {
        return id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public Double getRatingAverage() {
        return ratingAverage;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }
}
