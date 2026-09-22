package com.example.ecommerce.search;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;

import com.example.ecommerce.dto.ProductResponse;

import tools.jackson.databind.ObjectMapper;

@Service
public class ProductSearchService {

    private final ElasticsearchOperations operations;
    private final ObjectMapper objectMapper;

    public ProductSearchService(
            ElasticsearchOperations operations,
            ObjectMapper objectMapper) {

        this.operations = operations;
        this.objectMapper = objectMapper;
    }

    public Page<ProductResponse> search(
            String queryText,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating,
            Long vendorId,
            Pageable pageable) {

        try {

            List<Map<String, Object>> must =
                    new ArrayList<>();

            List<Map<String, Object>> filters =
                    new ArrayList<>();

            if (queryText != null
                    && !queryText.isBlank()) {

                Map<String, Object> multiMatch =
                        new LinkedHashMap<>();

                multiMatch.put(
                        "query",
                        queryText
                );

                multiMatch.put(
                        "fields",
                        List.of(
                                "name^3",
                                "category",
                                "vendorName"
                        )
                );

                multiMatch.put(
                        "fuzziness",
                        "AUTO"
                );

                must.add(
                        Map.of(
                                "multi_match",
                                multiMatch
                        )
                );
            }

            if (minPrice != null
                    || maxPrice != null) {

                Map<String, Object> priceRange =
                        new LinkedHashMap<>();

                if (minPrice != null) {

                    priceRange.put(
                            "gte",
                            minPrice.doubleValue()
                    );
                }

                if (maxPrice != null) {

                    priceRange.put(
                            "lte",
                            maxPrice.doubleValue()
                    );
                }

                filters.add(
                        Map.of(
                                "range",
                                Map.of(
                                        "price",
                                        priceRange
                                )
                        )
                );
            }

            if (minRating != null) {

                filters.add(
                        Map.of(
                                "range",
                                Map.of(
                                        "ratingAverage",
                                        Map.of(
                                                "gte",
                                                minRating
                                        )
                                )
                        )
                );
            }

            if (vendorId != null) {

                filters.add(
                        Map.of(
                                "term",
                                Map.of(
                                        "vendorId",
                                        vendorId
                                )
                        )
                );
            }

            Map<String, Object> bool =
                    new LinkedHashMap<>();

            if (must.isEmpty()) {

                bool.put(
                        "must",
                        List.of(
                                Map.of(
                                        "match_all",
                                        Map.of()
                                )
                        )
                );

            } else {

                bool.put(
                        "must",
                        must
                );
            }

            if (!filters.isEmpty()) {

                bool.put(
                        "filter",
                        filters
                );
            }

            Map<String, Object> queryBody =
                    new LinkedHashMap<>();

            queryBody.put(
                    "bool",
                    bool
            );

            String json =
                    objectMapper.writeValueAsString(
                            queryBody
                    );

            StringQuery query =
                    new StringQuery(json);

            query.setPageable(pageable);

            SearchHits<ProductDocument> hits =
                    operations.search(
                            query,
                            ProductDocument.class
                    );

            List<ProductResponse> content =
                    hits.getSearchHits()
                            .stream()
                            .map(
                                    hit ->
                                            toResponse(
                                                    hit.getContent()
                                            )
                            )
                            .toList();

            return new PageImpl<>(
                    content,
                    pageable,
                    hits.getTotalHits()
            );

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Unable to execute Elasticsearch product search",
                    ex
            );
        }
    }

    private ProductResponse toResponse(
            ProductDocument document) {

        return new ProductResponse(
                Long.valueOf(
                        document.getId()
                ),
                document.getName(),
                document.getCategory(),
                document.getPrice() != null
                        ? BigDecimal.valueOf(
                                document.getPrice()
                        )
                        : null,
                document.getStock(),
                document.getVendorId(),
                document.getVendorName(),
                document.getImageUrl(),
                document.getRatingAverage(),
                document.getReviewCount()
        );
    }
}