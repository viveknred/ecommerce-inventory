package com.example.ecommerce.specification;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.example.ecommerce.entity.Product;

/**
 * Reusable {@link Specification} fragments for dynamic product filtering.
 *
 * <p>Each method returns one independent predicate. The service starts from
 * {@code Specification.unrestricted()} and chains {@code .and(...)} only for the
 * filters the caller actually supplied, so unused filters never reach the
 * generated SQL.
 */
public class ProductSpecification {

    private ProductSpecification() {
        // Static factory holder, never instantiated.
    }

    public static Specification<Product> hasCategory(String category) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("category")),
                        "%" + category.toLowerCase() + "%"
                );
    }

    public static Specification<Product> priceGreaterThanOrEqualTo(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"),
                        minPrice
                );
    }

    public static Specification<Product> priceLessThanOrEqualTo(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"),
                        maxPrice
                );
    }

    public static Specification<Product> isInStock() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(
                        root.get("stock"),
                        0
                );
    }

    /**
     * Phase 4: restrict the catalogue to a single marketplace vendor.
     */
    public static Specification<Product> hasVendor(Long vendorId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("vendor").get("id"),
                        vendorId
                );
    }

    /**
     * Phase 4: only products whose recalculated average rating meets a floor.
     */
    public static Specification<Product> ratingAtLeast(Double minRating) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("ratingAverage"),
                        minRating
                );
    }
}
