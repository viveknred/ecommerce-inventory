package com.example.ecommerce.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.ecommerce.audit.AuditAction;
import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.specification.ProductSpecification;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    @AuditAction(
            entity = "Product",
            action = "STOCK_ADJUSTMENT")
    public ProductResponse create(ProductRequest request) {

        Product product = new Product();

        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product savedProduct = productRepository.save(product);

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

        if (category != null && !category.isBlank()) {
            specification = specification.and(
                    ProductSpecification.hasCategory(category)
            );
        }

        if (minPrice != null) {
            specification = specification.and(
                    ProductSpecification.priceGreaterThanOrEqualTo(minPrice)
            );
        }

        if (maxPrice != null) {
            specification = specification.and(
                    ProductSpecification.priceLessThanOrEqualTo(maxPrice)
            );
        }

        if (Boolean.TRUE.equals(inStock)) {
            specification = specification.and(
                    ProductSpecification.isInStock()
            );
        }

        return productRepository
                .findAll(specification, pageable)
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

    @Cacheable(value = "product", key = "#id")
    public ProductResponse getById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"
                        ));

        return toResponse(product);
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    @AuditAction(
            entity = "Product",
            action = "STOCK_ADJUSTMENT")
    public ProductResponse update(
            Long id,
            ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"
                        ));

        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product updatedProduct =
                productRepository.save(product);

        return toResponse(updatedProduct);
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    @AuditAction(
            entity = "Product",
            action = "STOCK_ADJUSTMENT")
    public ProductResponse adjustStock(
            Long id,
            Integer quantity) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"
                        ));

        int oldStock = product.getStock();

        product.setStock(
                oldStock + quantity
        );

        Product updatedProduct =
                productRepository.save(product);

        return toResponse(updatedProduct);
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    @AuditAction(
            entity = "Product",
            action = "STOCK_ADJUSTMENT")
    public void delete(Long id) {

        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Product not found"
            );
        }

        productRepository.deleteById(id);
    }

    private ProductResponse toResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStock()
        );
    }
}