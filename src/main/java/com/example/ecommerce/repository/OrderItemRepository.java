package com.example.ecommerce.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.OrderStatus;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Phase 4 review gate: true when the given user has at least one order in
     * one of the supplied statuses that contains the given product.
     */
    @Query("""
            SELECT COUNT(oi) > 0
            FROM OrderItem oi
            WHERE oi.order.user.id = :userId
              AND oi.product.id = :productId
              AND oi.order.status IN :statuses
            """)
    boolean hasPurchasedProduct(
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            @Param("statuses") Collection<OrderStatus> statuses
    );
}
