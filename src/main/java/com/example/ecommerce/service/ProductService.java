package com.example.ecommerce.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.Vendor;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.ReviewRepository;
import com.example.ecommerce.security.CurrentUserService;
import com.example.ecommerce.specification.ProductSpecification;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final VendorService vendorService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public ProductService(
            ProductRepository productRepository,
            ReviewRepository reviewRepository,
            VendorService vendorService,
            CurrentUserService currentUserService,
            AuditService auditService) {

        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.vendorService = vendorService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    /**
     * Creates a product.
     *
     * <p>Phase 4 scoping: ROLE_ADMIN must name the owning vendor explicitly,
     * while a ROLE_VENDOR account always has its own vendor id forced onto the
     * product regardless of what the request body says.
     */
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    @Transactional
    public ProductResponse create(ProductRequest request) {

        Vendor vendor =
                resolveVendorForWrite(request.getVendorId());

        Product product = new Product();

        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setVendor(vendor);
        product.setImageUrl(normalise(request.getImageUrl()));
        product.setRatingAverage(0.0);
        product.setReviewCount(0);

        Product savedProduct =
                productRepository.save(product);

        Map<String, Object> details =
                new HashMap<>();

        details.put("productId", savedProduct.getId());
        details.put("name", savedProduct.getName());
        details.put("category", savedProduct.getCategory());
        details.put("price", savedProduct.getPrice());
        details.put("stock", savedProduct.getStock());
        details.put("vendorId", vendor.getId());
        details.put("vendorName", vendor.getBusinessName());

        auditService.log(
                "Product",
                "PRODUCT_CREATED",
                details
        );

        return ProductResponse.from(savedProduct);
    }

    /**
     * Filtered, paginated catalogue.
     *
     * <p>{@code getProducts} and {@link #search} deliberately share the
     * {@code products} cache: they take the same arguments and return the same
     * result, so a hit populated by one is a valid hit for the other.
     */
    @Cacheable(value = "products")
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock,
            Long vendorId,
            Double minRating,
            Pageable pageable) {

        return queryProducts(
                category,
                minPrice,
                maxPrice,
                inStock,
                vendorId,
                minRating,
                pageable
        );
    }

    /**
     * Alias of {@link #getProducts} kept for the {@code /search} route.
     *
     * <p>Both entry points delegate to the private {@link #queryProducts}
     * helper. Previously {@code search} called {@code getProducts} directly,
     * which is a self-invocation: the call never left the object, so it bypassed
     * the Spring cache proxy and {@code @Cacheable} was silently ineffective for
     * every request that arrived through {@code /search}.
     */
    @Cacheable(value = "products")
    @Transactional(readOnly = true)
    public Page<ProductResponse> search(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock,
            Long vendorId,
            Double minRating,
            Pageable pageable) {

        return queryProducts(
                category,
                minPrice,
                maxPrice,
                inStock,
                vendorId,
                minRating,
                pageable
        );
    }

    /**
     * Phase 4, Module 2: {@code GET /api/v1/vendors/{id}/products}.
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getByVendor(
            Long vendorId,
            Pageable pageable) {

        // Surfaces a 404 for an unknown vendor rather than an empty page.
        vendorService.getEntity(vendorId);

        return productRepository
                .findByVendorId(vendorId, pageable)
                .map(ProductResponse::from);
    }

    @Cacheable(value = "product", key = "#p0")
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return ProductResponse.from(getEntity(id));
    }

    /**
     * Entity lookup shared with {@code ReviewService}.
     */
    @Transactional(readOnly = true)
    public Product getEntity(Long id) {

        return productRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + id
                        ));
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    @Transactional
    public ProductResponse update(
            Long id,
            ProductRequest request) {

        Product product = getEntity(id);

        assertCanManage(product);

        Vendor vendor =
                resolveVendorForWrite(request.getVendorId());

        // Captured before mutation. The previous implementation read oldPrice
        // off the same instance *after* the setters had run, so the audit log
        // recorded the new price as both the old and the new value.
        int oldStock = product.getStock();
        BigDecimal oldPrice = product.getPrice();

        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setVendor(vendor);

        if (request.getImageUrl() != null) {
            product.setImageUrl(normalise(request.getImageUrl()));
        }

        Product updatedProduct =
                productRepository.save(product);

        Map<String, Object> details =
                new HashMap<>();

        details.put("productId", updatedProduct.getId());
        details.put("oldStock", oldStock);
        details.put("newStock", updatedProduct.getStock());
        details.put("oldPrice", oldPrice);
        details.put("newPrice", updatedProduct.getPrice());
        details.put("vendorId", vendor.getId());

        auditService.log(
                "Product",
                "PRODUCT_UPDATED",
                details
        );

        return ProductResponse.from(updatedProduct);
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    @Transactional
    public ProductResponse adjustStock(
            Long id,
            Integer quantity) {

        Product product = getEntity(id);

        int oldStock = product.getStock();
        int newStock = oldStock + quantity;

        product.setStock(newStock);

        Product updatedProduct =
                productRepository.save(product);

        Map<String, Object> details =
                new HashMap<>();

        details.put("productId", product.getId());
        details.put("productName", product.getName());
        details.put("oldStock", oldStock);
        details.put("quantityChanged", quantity);
        details.put("newStock", newStock);

        String action =
                quantity < 0
                        ? "STOCK_DEDUCTED"
                        : "STOCK_RESTORED";

        auditService.log(
                "Product",
                action,
                details
        );

        return ProductResponse.from(updatedProduct);
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    @Transactional
    public void delete(Long id) {

        Product product = getEntity(id);

        assertCanManage(product);

        Map<String, Object> details =
                new HashMap<>();

        details.put("productId", product.getId());
        details.put("name", product.getName());
        details.put("price", product.getPrice());
        details.put("stock", product.getStock());

        if (product.getVendor() != null) {
            details.put("vendorId", product.getVendor().getId());
        }

        productRepository.deleteById(id);

        auditService.log(
                "Product",
                "PRODUCT_DELETED",
                details
        );
    }

    /**
     * Phase 4, Module 3: recomputes the denormalised rating columns on one
     * product from its reviews. Called after every new review.
     *
     * @return the recalculated average, or {@code 0.0} when the last review was
     *         removed
     */
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    @Transactional
    public double recalculateRatingAverage(Long productId) {

        Product product = getEntity(productId);

        Double average =
                reviewRepository
                        .findAverageRatingByProductId(productId);

        long count =
                reviewRepository.countByProductId(productId);

        double rounded =
                average == null
                        ? 0.0
                        : Math.round(average * 100.0) / 100.0;

        product.setRatingAverage(rounded);
        product.setReviewCount((int) count);

        productRepository.save(product);

        return rounded;
    }

    /**
     * Composes only the filters the caller actually supplied, so unused
     * parameters never reach the generated SQL.
     */
    private Page<ProductResponse> queryProducts(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock,
            Long vendorId,
            Double minRating,
            Pageable pageable) {

        Specification<Product> specification =
                Specification.unrestricted();

        if (category != null
                && !category.isBlank()) {

            specification =
                    specification.and(
                            ProductSpecification.hasCategory(category)
                    );
        }

        if (minPrice != null) {

            specification =
                    specification.and(
                            ProductSpecification
                                    .priceGreaterThanOrEqualTo(minPrice)
                    );
        }

        if (maxPrice != null) {

            specification =
                    specification.and(
                            ProductSpecification
                                    .priceLessThanOrEqualTo(maxPrice)
                    );
        }

        if (Boolean.TRUE.equals(inStock)) {

            specification =
                    specification.and(
                            ProductSpecification.isInStock()
                    );
        }

        if (vendorId != null) {

            specification =
                    specification.and(
                            ProductSpecification.hasVendor(vendorId)
                    );
        }

        if (minRating != null) {

            specification =
                    specification.and(
                            ProductSpecification.ratingAtLeast(minRating)
                    );
        }

        return productRepository
                .findAll(specification, pageable)
                .map(ProductResponse::from);
    }

    /**
     * Decides which vendor a write may target.
     *
     * <p>A ROLE_VENDOR caller is pinned to its own vendor and cannot name
     * another one. A ROLE_ADMIN caller must name a vendor, because in the
     * marketplace model every new product belongs to a seller.
     */
    private Vendor resolveVendorForWrite(Long requestedVendorId) {

        User currentUser =
                currentUserService.requireCurrentUser();

        if (currentUser.getRole() == Role.VENDOR) {

            Vendor ownVendor = currentUser.getVendor();

            if (ownVendor == null) {

                throw new AccessDeniedException(
                        "Your account is not linked to a vendor. "
                                + "Ask an administrator to link it before "
                                + "managing products."
                );
            }

            if (requestedVendorId != null
                    && !requestedVendorId.equals(ownVendor.getId())) {

                throw new AccessDeniedException(
                        "You may only manage products for vendor "
                                + ownVendor.getId()
                                + " ("
                                + ownVendor.getBusinessName()
                                + ")"
                );
            }

            return ownVendor;
        }

        if (requestedVendorId == null) {

            throw new IllegalArgumentException(
                    "vendorId is required: every product must belong to "
                            + "a vendor. Create one via POST /api/v1/vendors."
            );
        }

        return vendorService.getEntity(requestedVendorId);
    }

    /**
     * Rejects a ROLE_VENDOR caller touching a product it does not own.
     */
    private void assertCanManage(Product product) {

        User currentUser =
                currentUserService.requireCurrentUser();

        if (currentUser.getRole() != Role.VENDOR) {
            return;
        }

        Vendor ownVendor = currentUser.getVendor();

        if (ownVendor == null) {

            throw new AccessDeniedException(
                    "Your account is not linked to a vendor"
            );
        }

        boolean ownsProduct =
                product.getVendor() != null
                        && ownVendor.getId()
                        .equals(product.getVendor().getId());

        if (!ownsProduct) {

            throw new AccessDeniedException(
                    "Product "
                            + product.getId()
                            + " belongs to another vendor and cannot "
                            + "be modified by your account"
            );
        }
    }

    private String normalise(String value) {

        return Optional
                .ofNullable(value)
                .map(String::trim)
                .filter(trimmed -> !trimmed.isEmpty())
                .orElse(null);
    }
}
