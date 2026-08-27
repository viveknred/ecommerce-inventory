package com.example.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ecommerce.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductId(Long productId, Pageable pageable);

    Optional<Review> findByProductIdAndUserId(Long productId, Long userId);

    boolean existsByProductIdAndUserId(Long productId, Long userId);

    long countByProductId(Long productId);

    /**
     * Average rating across every review of one product.
     * Returns null when the product has no reviews yet.
     */
    @Query("""
            SELECT AVG(r.rating)
            FROM Review r
            WHERE r.product.id = :productId
            """)
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    /**
     * Average rating across every review of every product owned by one vendor.
     * Weighted by review volume rather than an average of product averages.
     * Returns null when the vendor has no reviews yet.
     */
    @Query("""
            SELECT AVG(r.rating)
            FROM Review r
            WHERE r.product.vendor.id = :vendorId
            """)
    Double findAverageRatingByVendorId(@Param("vendorId") Long vendorId);

    /**
     * Star distribution for one product as {rating, count} rows.
     * Ratings with no reviews are absent and default to zero in the service.
     */
    @Query("""
            SELECT r.rating, COUNT(r)
            FROM Review r
            WHERE r.product.id = :productId
            GROUP BY r.rating
            """)
    List<Object[]> countGroupedByRating(@Param("productId") Long productId);
}
