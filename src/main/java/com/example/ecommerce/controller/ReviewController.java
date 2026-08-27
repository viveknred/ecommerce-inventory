package com.example.ecommerce.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.dto.ProductReviewsResponse;
import com.example.ecommerce.dto.ReviewRequest;
import com.example.ecommerce.dto.ReviewResponse;
import com.example.ecommerce.service.ReviewService;
import com.example.ecommerce.util.PageableFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/products/{productId}/reviews")
@Tag(
        name = "Reviews",
        description = "Verified-purchase product reviews and rating aggregates"
)
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Submits a review. Customers only, and only for products they have
     * actually bought: a request from a customer with no paid or shipped order
     * for the product is rejected with 403 Forbidden.
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Submit a product review",
            description = "ROLE_CUSTOMER only. Requires a prior paid or "
                    + "shipped order containing the product, otherwise "
                    + "403 Forbidden. One review per customer per product."
    )
    public ReviewResponse create(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody ReviewRequest request) {

        return reviewService.create(productId, request);
    }

    /**
     * Paginated reviews plus aggregate metrics: average rating, total review
     * count and the per-star breakdown.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'VENDOR', 'ADMIN')")
    @Operation(
            summary = "List product reviews with aggregate metrics",
            description = "Paginated. Returns averageRating, totalReviews and "
                    + "a 5-to-1 star ratingBreakdown alongside the page."
    )
    public ProductReviewsResponse getProductReviews(

            @PathVariable("productId") Long productId,

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
                        "createdAt",
                        Sort.Direction.DESC
                );

        return reviewService.getProductReviews(productId, pageable);
    }
}
