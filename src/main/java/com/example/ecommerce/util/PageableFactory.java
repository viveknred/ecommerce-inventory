package com.example.ecommerce.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Builds a {@link Pageable} from the raw {@code page}, {@code size} and
 * {@code sort} query parameters.
 *
 * <p>Phase 4 added three more paginated endpoints (vendor list, vendor
 * products, product reviews), so this logic was lifted out of
 * {@code ProductController} rather than copied four times. Sort values use the
 * Spring Data convention {@code property,direction}, for example
 * {@code ?sort=price,desc&sort=name,asc}.
 */
public final class PageableFactory {

    private static final int MAX_PAGE_SIZE = 100;

    private PageableFactory() {
        // Static helper, never instantiated.
    }

    /**
     * @param page          zero-based page index
     * @param size          page size, capped at {@value #MAX_PAGE_SIZE}
     * @param sort          raw {@code property,direction} values, may be null
     * @param defaultSortBy property to sort by when the caller supplied none
     */
    public static Pageable build(
            int page,
            int size,
            List<String> sort,
            String defaultSortBy) {

        return build(
                page,
                size,
                sort,
                defaultSortBy,
                Sort.Direction.ASC
        );
    }

    /**
     * As {@link #build(int, int, List, String)} but lets the caller choose the
     * fallback direction. Reviews, for instance, read best newest-first.
     */
    public static Pageable build(
            int page,
            int size,
            List<String> sort,
            String defaultSortBy,
            Sort.Direction defaultDirection) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be greater than or equal to 0"
            );
        }

        if (size < 1) {
            throw new IllegalArgumentException(
                    "Size must be greater than 0"
            );
        }

        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Size must not exceed " + MAX_PAGE_SIZE
            );
        }

        List<Sort.Order> orders = new ArrayList<>();

        if (sort != null) {

            for (String sortValue : sort) {

                if (sortValue == null
                        || sortValue.isBlank()) {
                    continue;
                }

                String[] parts =
                        sortValue.split(",");

                String property =
                        parts[0].trim();

                if (property.isBlank()) {
                    continue;
                }

                Sort.Direction direction =
                        Sort.Direction.ASC;

                if (parts.length > 1
                        && !parts[1].isBlank()) {

                    direction =
                            Sort.Direction.fromString(
                                    parts[1].trim()
                            );
                }

                orders.add(
                        new Sort.Order(
                                direction,
                                property
                        )
                );
            }
        }

        if (orders.isEmpty()) {

            orders.add(
                    new Sort.Order(
                            defaultDirection,
                            defaultSortBy
                    )
            );
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(orders)
        );
    }
}
