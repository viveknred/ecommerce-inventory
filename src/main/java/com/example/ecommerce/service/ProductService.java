package com.example.ecommerce.service;

import java.math.BigDecimal;
import java.util.List;

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

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse create(ProductRequest request) {
        Product product = new Product();

        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product savedProduct = productRepository.save(product);

        return new ProductResponse(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getCategory(),
                savedProduct.getPrice(),
                savedProduct.getStock()
        );
    }

    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream()
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getCategory(),
                        product.getPrice(),
                        product.getStock()
                ))
                .toList();
    }

    public Page<ProductResponse> search(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock,
            Pageable pageable) {

        Specification<Product> specification = Specification.unrestricted();

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

        return productRepository.findAll(specification, pageable)
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getCategory(),
                        product.getPrice(),
                        product.getStock()
                ));
    }

    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStock()
        );
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }

        productRepository.deleteById(id);
    }
}