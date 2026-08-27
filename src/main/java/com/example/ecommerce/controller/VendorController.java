package com.example.ecommerce.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.dto.VendorRequest;
import com.example.ecommerce.dto.VendorResponse;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.VendorService;
import com.example.ecommerce.util.PageableFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/vendors")
@Tag(
        name = "Vendors",
        description = "Marketplace vendor onboarding and catalogue browsing"
)
public class VendorController {

    private final VendorService vendorService;
    private final ProductService productService;

    public VendorController(
            VendorService vendorService,
            ProductService productService) {

        this.vendorService = vendorService;
        this.productService = productService;
    }

    /**
     * Onboards a vendor. Administrators only.
     *
     * <p>Supplying {@code ownerEmail} promotes that already-registered account
     * to ROLE_VENDOR and binds it to this vendor, after which the account may
     * manage only this vendor's products.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a vendor",
            description = "ROLE_ADMIN only. Optionally links an existing user "
                    + "as the vendor owner, promoting them to ROLE_VENDOR."
    )
    public VendorResponse create(
            @Valid @RequestBody VendorRequest request) {

        return vendorService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'VENDOR', 'ADMIN')")
    @Operation(summary = "List vendors")
    public Page<VendorResponse> getVendors(

            @RequestParam(name = "page", defaultValue = "0")
            int page,

            @RequestParam(name = "size", defaultValue = "10")
            int size,

            @RequestParam(name = "sort", required = false)
            List<String> sort) {

        Pageable pageable =
                PageableFactory.build(
                        page,
                        size,
                        sort,
                        "businessName"
                );

        return vendorService.getVendors(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'VENDOR', 'ADMIN')")
    @Operation(summary = "Get one vendor")
    public VendorResponse getById(
            @PathVariable("id") Long id) {

        return vendorService.getById(id);
    }

    /**
     * Paginated catalogue for a single vendor, as required by Module 2.
     */
    @GetMapping("/{id}/products")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'VENDOR', 'ADMIN')")
    @Operation(
            summary = "List a vendor's products",
            description = "Paginated. Supports ?page, ?size and "
                    + "?sort=property,direction."
    )
    public Page<ProductResponse> getVendorProducts(

            @PathVariable("id") Long id,

            @RequestParam(name = "page", defaultValue = "0")
            int page,

            @RequestParam(name = "size", defaultValue = "10")
            int size,

            @RequestParam(name = "sort", required = false)
            List<String> sort) {

        Pageable pageable =
                PageableFactory.build(
                        page,
                        size,
                        sort,
                        "name"
                );

        return productService.getByVendor(id, pageable);
    }

    @PatchMapping("/{id}/verification")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Verify or unverify a vendor",
            description = "ROLE_ADMIN only."
    )
    public VendorResponse verify(
            @PathVariable("id") Long id,
            @RequestParam(name = "verified", defaultValue = "true")
            boolean verified) {

        return vendorService.verify(id, verified);
    }
}
