package com.example.ecommerce.search;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.dto.ProductResponse;

@RestController
@RequestMapping("/api/v1/search")
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    public ProductSearchController(
            ProductSearchService productSearchService) {

        this.productSearchService =
                productSearchService;
    }

    @GetMapping("/products")
    @PreAuthorize(
            "hasAnyRole('CUSTOMER', 'VENDOR', 'ADMIN')"
    )
    public Page<ProductResponse> searchProducts(

            @RequestParam(
                    name = "q",
                    required = false
            )
            String query,

            @RequestParam(
                    name = "minPrice",
                    required = false
            )
            BigDecimal minPrice,

            @RequestParam(
                    name = "maxPrice",
                    required = false
            )
            BigDecimal maxPrice,

            @RequestParam(
                    name = "minRating",
                    required = false
            )
            Double minRating,

            @RequestParam(
                    name = "vendorId",
                    required = false
            )
            Long vendorId,

            @RequestParam(
                    name = "page",
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    name = "size",
                    defaultValue = "10"
            )
            int size) {

        if (page < 0) {
            page = 0;
        }

        if (size < 1) {
            size = 10;
        }

        if (size > 100) {
            size = 100;
        }

        return productSearchService.search(
                query,
                minPrice,
                maxPrice,
                minRating,
                vendorId,
                PageRequest.of(
                        page,
                        size
                )
        );
    }
}