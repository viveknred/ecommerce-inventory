package com.example.ecommerce.search;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.ecommerce.repository.ProductRepository;

@Component
public class ProductSearchBootstrap {

    private final ProductRepository productRepository;
    private final ProductSearchIndexer productSearchIndexer;
    private final org.springframework.data.elasticsearch.core.ElasticsearchOperations operations;

    public ProductSearchBootstrap(
            ProductRepository productRepository,
            ProductSearchIndexer productSearchIndexer,
            org.springframework.data.elasticsearch.core.ElasticsearchOperations operations) {

        this.productRepository = productRepository;
        this.productSearchIndexer = productSearchIndexer;
        this.operations = operations;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeSearchIndex() {

        try {

            var indexOperations =
                    operations.indexOps(
                            ProductDocument.class
                    );

            if (!indexOperations.exists()) {
                indexOperations.createWithMapping();
            }

            productRepository
                    .findAll()
                    .forEach(
                            productSearchIndexer::index
                    );

        } catch (Exception ignored) {
        }
    }
}