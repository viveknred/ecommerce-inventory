package com.example.ecommerce.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(
            @Valid @RequestBody ProductRequest request) {

        return productService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public Page<ProductResponse> getProducts(
            @RequestParam(
                    name = "category",
                    required = false
            )
            String category,

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
                    name = "inStock",
                    required = false
            )
            Boolean inStock,

            @RequestParam(
                    name = "page",
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    name = "size",
                    defaultValue = "10"
            )
            int size,

            @RequestParam(
                    name = "sort",
                    required = false
            )
            List<String> sort) {

        Pageable pageable = buildPageable(
                page,
                size,
                sort
        );

        return productService.getProducts(
                category,
                minPrice,
                maxPrice,
                inStock,
                pageable
        );
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public Page<ProductResponse> search(
            @RequestParam(
                    name = "category",
                    required = false
            )
            String category,

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
                    name = "inStock",
                    required = false
            )
            Boolean inStock,

            @RequestParam(
                    name = "page",
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    name = "size",
                    defaultValue = "10"
            )
            int size,

            @RequestParam(
                    name = "sort",
                    required = false
            )
            List<String> sort) {

        Pageable pageable = buildPageable(
                page,
                size,
                sort
        );

        return productService.search(
                category,
                minPrice,
                maxPrice,
                inStock,
                pageable
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ProductResponse getById(
            @PathVariable("id") Long id) {

        return productService.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse update(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProductRequest request) {

        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("id") Long id) {

        productService.delete(id);
    }

    private Pageable buildPageable(
            int page,
            int size,
            List<String> sort) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be greater than or equal to 0"
            );
        }

        if (size < 1) {
            throw new IllegalArgumentException(
                    "Size must be greater than 0"
            );
        }

        List<Sort.Order> orders = new ArrayList<>();

        if (sort != null) {

            for (String sortValue : sort) {

                if (sortValue == null
                        || sortValue.isBlank()) {
                    continue;
                }

                String[] parts =
                        sortValue.split(",");

                String property =
                        parts[0].trim();

                if (property.isBlank()) {
                    continue;
                }

                Sort.Direction direction =
                        Sort.Direction.ASC;

                if (parts.length > 1
                        && !parts[1].isBlank()) {

                    direction =
                            Sort.Direction.fromString(
                                    parts[1].trim()
                            );
                }

                orders.add(
                        new Sort.Order(
                                direction,
                                property
                        )
                );
            }
        }

        if (orders.isEmpty()) {

            orders.add(
                    new Sort.Order(
                            Sort.Direction.ASC,
                            "name"
                    )
            );
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(orders)
        );
    }
}