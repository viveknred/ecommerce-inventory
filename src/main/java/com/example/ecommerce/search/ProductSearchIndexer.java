package com.example.ecommerce.search;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import com.example.ecommerce.entity.Product;

@Service
public class ProductSearchIndexer {

    private final ElasticsearchOperations operations;

    public ProductSearchIndexer(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    public void index(Product product) {

        Long vendorId =
                product.getVendor() != null
                        ? product.getVendor().getId()
                        : null;

        String vendorName =
                product.getVendor() != null
                        ? product.getVendor().getBusinessName()
                        : null;

        ProductDocument document =
                new ProductDocument(
                        String.valueOf(product.getId()),
                        product.getName(),
                        product.getCategory(),
                        product.getPrice(),
                        product.getStock(),
                        vendorId,
                        vendorName,
                        product.getRatingAverage(),
                        product.getReviewCount(),
                        product.getImageUrl()
                );

        operations.save(document);
    }

    public void delete(Long productId) {
        operations.delete(
                String.valueOf(productId),
                ProductDocument.class
        );
    }
}