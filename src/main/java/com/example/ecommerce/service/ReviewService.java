package com.example.ecommerce.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.dto.ProductReviewsResponse;
import com.example.ecommerce.dto.ReviewRequest;
import com.example.ecommerce.dto.ReviewResponse;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.Review;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ReviewNotAllowedException;
import com.example.ecommerce.repository.OrderItemRepository;
import com.example.ecommerce.repository.ReviewRepository;
import com.example.ecommerce.security.CurrentUserService;

/**
 * Phase 4, Module 3: verified-purchase reviews and the rating engine.
 *
 * <p>A review is only accepted when the caller has an order containing that
 * product which has progressed past payment. The domain has no COMPLETED
 * status, so {@link #PURCHASED_STATUSES} treats PAID and SHIPPED as proof of a
 * completed purchase; PENDING (unpaid) and CANCELLED do not qualify.
 *
 * <p>Every accepted review triggers a recalculation of the average rating on
 * both the product and its parent vendor.
 */
@Service
public class ReviewService {

    /**
     * Order states that count as a completed purchase for review eligibility.
     */
    private static final Set<OrderStatus> PURCHASED_STATUSES =
            Set.of(OrderStatus.PAID, OrderStatus.SHIPPED);

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final VendorService vendorService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public ReviewService(
            ReviewRepository reviewRepository,
            OrderItemRepository orderItemRepository,
            ProductService productService,
            VendorService vendorService,
            CurrentUserService currentUserService,
            AuditService auditService) {

        this.reviewRepository = reviewRepository;
        this.orderItemRepository = orderItemRepository;
        this.productService = productService;
        this.vendorService = vendorService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    /**
     * Records a review for a product the caller has actually bought.
     *
     * @throws ReviewNotAllowedException when the caller has no qualifying order
     *                                   for the product, or has already
     *                                   reviewed it
     */
    @Transactional
    public ReviewResponse create(
            Long productId,
            ReviewRequest request) {

        User author =
                currentUserService.requireCurrentUser();

        Product product =
                productService.getEntity(productId);

        boolean hasPurchased =
                orderItemRepository.hasPurchasedProduct(
                        author.getId(),
                        productId,
                        PURCHASED_STATUSES
                );

        if (!hasPurchased) {

            throw new ReviewNotAllowedException(
                    "You can only review products you have purchased. "
                            + "No paid or shipped order was found for product "
                            + productId
                            + " on your account."
            );
        }

        boolean alreadyReviewed =
                reviewRepository.existsByProductIdAndUserId(
                        productId,
                        author.getId()
                );

        if (alreadyReviewed) {

            throw new ReviewNotAllowedException(
                    "You have already reviewed product "
                            + productId
                            + ". Each customer may leave one review "
                            + "per product."
            );
        }

        Review review = new Review();

        review.setProduct(product);
        review.setUser(author);
        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());
        review.setImageUrl(normalise(request.getImageUrl()));

        Review savedReview =
                reviewRepository.save(review);

        // Both aggregates refresh on every new review, as Module 3 requires.
        double productAverage =
                productService.recalculateRatingAverage(productId);

        Double vendorAverage = null;

        if (product.getVendor() != null) {

            vendorAverage =
                    vendorService.recalculateRatingAverage(
                            product.getVendor().getId()
                    );
        }

        Map<String, Object> details =
                new HashMap<>();

        details.put("reviewId", savedReview.getId());
        details.put("productId", productId);
        details.put("productName", product.getName());
        details.put("userId", author.getId());
        details.put("email", author.getEmail());
        details.put("rating", savedReview.getRating());
        details.put("productRatingAverage", productAverage);

        if (product.getVendor() != null) {
            details.put("vendorId", product.getVendor().getId());
            details.put("vendorRatingAverage", vendorAverage);
        }

        auditService.log(
                "Review",
                "REVIEW_CREATED",
                details
        );

        return toResponse(savedReview);
    }

    /**
     * Paginated reviews for one product, alongside the aggregate metrics
     * required by Module 3: average rating, total review count, and the
     * per-star distribution.
     */
    @Transactional(readOnly = true)
    public ProductReviewsResponse getProductReviews(
            Long productId,
            Pageable pageable) {

        Product product =
                productService.getEntity(productId);

        Page<ReviewResponse> reviews =
                reviewRepository
                        .findByProductId(productId, pageable)
                        .map(this::toResponse);

        Double average =
                reviewRepository
                        .findAverageRatingByProductId(productId);

        long totalReviews =
                reviewRepository.countByProductId(productId);

        return new ProductReviewsResponse(
                productId,
                product.getName(),
                average == null
                        ? 0.0
                        : Math.round(average * 100.0) / 100.0,
                totalReviews,
                buildRatingBreakdown(productId),
                reviews
        );
    }

    /**
     * Star distribution from 5 down to 1, with explicit zeros so that clients
     * can render a full histogram without null checks.
     */
    private Map<Integer, Long> buildRatingBreakdown(Long productId) {

        Map<Integer, Long> breakdown =
                new LinkedHashMap<>();

        for (int star = 5; star >= 1; star--) {
            breakdown.put(star, 0L);
        }

        List<Object[]> rows =
                reviewRepository.countGroupedByRating(productId);

        for (Object[] row : rows) {

            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];

            breakdown.put(rating, count);
        }

        return breakdown;
    }

    private ReviewResponse toResponse(Review review) {

        return new ReviewResponse(
                review.getId(),
                review.getProduct().getId(),
                review.getUser().getId(),
                review.getUser().getName(),
                review.getRating(),
                review.getComment(),
                review.getImageUrl(),
                review.getCreatedAt()
        );
    }

    private String normalise(String value) {

        return Optional
                .ofNullable(value)
                .map(String::trim)
                .filter(trimmed -> !trimmed.isEmpty())
                .orElse(null);
    }
}
