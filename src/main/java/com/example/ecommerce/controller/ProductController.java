package com.example.ecommerce.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.util.PageableFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/products")
@Tag(
        name = "Products",
        description = "Catalogue management and filtered browsing"
)
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Phase 4: ROLE_VENDOR may create products too, but only ever under its
     * own vendor id. ROLE_ADMIN must state the target vendor in the body.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a product",
            description = "ROLE_ADMIN must supply vendorId. ROLE_VENDOR has "
                    + "its own vendor id applied automatically."
    )
    public ProductResponse create(
            @Valid @RequestBody ProductRequest request) {

        return productService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'VENDOR', 'ADMIN')")
    @Operation(summary = "List products with optional filters")
    public Page<ProductResponse> getProducts(

            @RequestParam(name = "category", required = false)
            String category,

            @RequestParam(name = "minPrice", required = false)
            BigDecimal minPrice,

            @RequestParam(name = "maxPrice", required = false)
            BigDecimal maxPrice,

            @RequestParam(name = "inStock", required = false)
            Boolean inStock,

            @RequestParam(name = "vendorId", required = false)
            Long vendorId,

            @RequestParam(name = "minRating", required = false)
            Double minRating,

            @RequestParam(name = "page", defaultValue = "0")
            int page,

            @RequestParam(name = "size", defaultValue = "10")
            int size,

            @RequestParam(name = "sort", required = false)
            List<String> sort) {

        Pageable pageable =
                PageableFactory.build(page, size, sort, "name");

        return productService.getProducts(
                category,
                minPrice,
                maxPrice,
                inStock,
                vendorId,
                minRating,
                pageable
        );
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'VENDOR', 'ADMIN')")
    @Operation(summary = "Search products with optional filters")
    public Page<ProductResponse> search(

            @RequestParam(name = "category", required = false)
            String category,

            @RequestParam(name = "minPrice", required = false)
            BigDecimal minPrice,

            @RequestParam(name = "maxPrice", required = false)
            BigDecimal maxPrice,

            @RequestParam(name = "inStock", required = false)
            Boolean inStock,

            @RequestParam(name = "vendorId", required = false)
            Long vendorId,

            @RequestParam(name = "minRating", required = false)
            Double minRating,

            @RequestParam(name = "page", defaultValue = "0")
            int page,

            @RequestParam(name = "size", defaultValue = "10")
            int size,

            @RequestParam(name = "sort", required = false)
            List<String> sort) {

        Pageable pageable =
                PageableFactory.build(page, size, sort, "name");

        return productService.search(
                category,
                minPrice,
                maxPrice,
                inStock,
                vendorId,
                minRating,
                pageable
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'VENDOR', 'ADMIN')")
    @Operation(summary = "Get one product")
    public ProductResponse getById(
            @PathVariable("id") Long id) {

        return productService.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @Operation(
            summary = "Update a product",
            description = "ROLE_VENDOR may only update products belonging to "
                    + "its own vendor id; otherwise 403 Forbidden."
    )
    public ProductResponse update(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProductRequest request) {

        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a product",
            description = "ROLE_VENDOR may only delete products belonging to "
                    + "its own vendor id; otherwise 403 Forbidden."
    )
    public void delete(
            @PathVariable("id") Long id) {

        productService.delete(id);
    }
}
