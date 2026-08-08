package com.example.ecommerce.specification;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.example.ecommerce.entity.Product;

public class ProductSpecification {

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
}