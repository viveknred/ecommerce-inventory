package com.example.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class VendorRequest {

    @NotBlank
    private String businessName;

    @Email
    @NotBlank
    private String supportEmail;

    /**
     * Optional. Public URL returned by POST /api/v1/media/upload.
     */
    private String logoUrl;

    /**
     * Optional. Email of an existing user account to become the owner of this
     * vendor. That account is promoted to ROLE_VENDOR and bound to this vendor.
     */
    @Email
    private String ownerEmail;

    public VendorRequest() {
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}
