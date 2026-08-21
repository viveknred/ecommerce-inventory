package com.example.ecommerce.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.specification.ProductSpecification;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final AuditService auditService;

    public ProductService(
            ProductRepository productRepository,
            AuditService auditService) {

        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ProductResponse create(ProductRequest request) {

        Product product = new Product();

        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product savedProduct =
                productRepository.save(product);

        Map<String, Object> details =
                new HashMap<>();

        details.put("productId", savedProduct.getId());
        details.put("name", savedProduct.getName());
        details.put("price", savedProduct.getPrice());
        details.put("stock", savedProduct.getStock());

        auditService.log(
                "Product",
                "PRODUCT_CREATED",
                details
        );

        return toResponse(savedProduct);
    }

    @Cacheable(value = "products")
    public Page<ProductResponse> getProducts(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock,
            Pageable pageable) {

        Specification<Product> specification =
                Specification.unrestricted();

        if (category != null
                && !category.isBlank()) {

            specification =
                    specification.and(
                            ProductSpecification
                                    .hasCategory(category)
                    );
        }

        if (minPrice != null) {

            specification =
                    specification.and(
                            ProductSpecification
                                    .priceGreaterThanOrEqualTo(
                                            minPrice
                                    )
                    );
        }

        if (maxPrice != null) {

            specification =
                    specification.and(
                            ProductSpecification
                                    .priceLessThanOrEqualTo(
                                            maxPrice
                                    )
                    );
        }

        if (Boolean.TRUE.equals(inStock)) {

            specification =
                    specification.and(
                            ProductSpecification
                                    .isInStock()
                    );
        }

        return productRepository
                .findAll(
                        specification,
                        pageable
                )
                .map(this::toResponse);
    }

    public Page<ProductResponse> search(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock,
            Pageable pageable) {

        return getProducts(
                category,
                minPrice,
                maxPrice,
                inStock,
                pageable
        );
    }

    @Cacheable(
            value = "product",
            key = "#id"
    )
    public ProductResponse getById(Long id) {

        Product product =
                productRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product not found"
                                ));

        return toResponse(product);
    }

    @CacheEvict(
            value = {"products", "product"},
            allEntries = true
    )
    public ProductResponse update(
            Long id,
            ProductRequest request) {

        Product product =
                productRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product not found"
                                ));

        int oldStock = product.getStock();

        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product updatedProduct =
                productRepository.save(product);

        Map<String, Object> details =
                new HashMap<>();

        details.put(
                "productId",
                updatedProduct.getId()
        );

        details.put(
                "oldStock",
                oldStock
        );

        details.put(
                "newStock",
                updatedProduct.getStock()
        );

        details.put(
                "oldPrice",
                product.getPrice()
        );

        details.put(
                "newPrice",
                updatedProduct.getPrice()
        );

        auditService.log(
                "Product",
                "PRODUCT_UPDATED",
                details
        );

        return toResponse(updatedProduct);
    }

    @CacheEvict(
            value = {"products", "product"},
            allEntries = true
    )
    public ProductResponse adjustStock(
            Long id,
            Integer quantity) {

        Product product =
                productRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product not found"
                                ));

        int oldStock =
                product.getStock();

        int newStock =
                oldStock + quantity;

        product.setStock(newStock);

        Product updatedProduct =
                productRepository.save(product);

        Map<String, Object> details =
                new HashMap<>();

        details.put(
                "productId",
                product.getId()
        );

        details.put(
                "productName",
                product.getName()
        );

        details.put(
                "oldStock",
                oldStock
        );

        details.put(
                "quantityChanged",
                quantity
        );

        details.put(
                "newStock",
                newStock
        );

        String action =
                quantity < 0
                        ? "STOCK_DEDUCTED"
                        : "STOCK_RESTORED";

        auditService.log(
                "Product",
                action,
                details
        );

        return toResponse(updatedProduct);
    }

    @CacheEvict(
            value = {"products", "product"},
            allEntries = true
    )
    public void delete(Long id) {

        Product product =
                productRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product not found"
                                ));

        Map<String, Object> details =
                new HashMap<>();

        details.put(
                "productId",
                product.getId()
        );

        details.put(
                "name",
                product.getName()
        );

        details.put(
                "price",
                product.getPrice()
        );

        details.put(
                "stock",
                product.getStock()
        );

        productRepository.deleteById(id);

        auditService.log(
                "Product",
                "PRODUCT_DELETED",
                details
        );
    }

    private ProductResponse toResponse(
            Product product) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStock()
        );
    }
}