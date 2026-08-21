package com.example.ecommerce.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.ecommerce.dto.OrderItemRequest;
import com.example.ecommerce.dto.OrderRequest;
import com.example.ecommerce.entity.Coupon;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.event.OrderCreatedEvent;
import com.example.ecommerce.event.OrderCreatedEventPublisher;
import com.example.ecommerce.event.OrderCreatedItem;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.InvalidStateTransitionException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CouponService couponService;
    private final AuditService auditService;
    private final OrderCreatedEventPublisher orderCreatedEventPublisher;
    private final Counter revenueCounter;

    public OrderService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            ProductService productService,
            CouponService couponService,
            AuditService auditService,
            OrderCreatedEventPublisher orderCreatedEventPublisher,
            MeterRegistry meterRegistry) {

        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.couponService = couponService;
        this.auditService = auditService;
        this.orderCreatedEventPublisher =
                orderCreatedEventPublisher;

        this.revenueCounter =
                Counter.builder("orders.revenue.total")
                        .description(
                                "Total revenue value from successfully completed orders"
                        )
                        .register(meterRegistry);
    }

    @Transactional
    public Order create(OrderRequest request) {

        User user =
                userRepository
                        .findById(request.getUserId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        return createOrder(
                request,
                user
        );
    }

    @Transactional
    public Order createForAuthenticatedUser(
            OrderRequest request,
            String email) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        if (!user.getId().equals(request.getUserId())) {

            throw new IllegalArgumentException(
                    "User ID does not match authenticated user"
            );
        }

        return createOrder(
                request,
                user
        );
    }

    private Order createOrder(
            OrderRequest request,
            User user) {

        Order order = new Order();

        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount =
                BigDecimal.ZERO;

        List<OrderItem> items =
                new ArrayList<>();

        List<Long> productIds =
                new ArrayList<>();

        List<Integer> quantities =
                new ArrayList<>();

        for (OrderItemRequest itemRequest
                : request.getItems()) {

            Product product =
                    productRepository
                            .findById(
                                    itemRequest.getProductId()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Product not found: "
                                                    + itemRequest
                                                            .getProductId()
                                    )
                            );

            if (product.getStock()
                    < itemRequest.getQuantity()) {

                throw new InsufficientStockException(
                        "Insufficient stock for product: "
                                + product.getName()
                                + ". Requested: "
                                + itemRequest.getQuantity()
                                + ", Available: "
                                + product.getStock()
                );
            }

            BigDecimal itemTotal =
                    product.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            itemRequest
                                                    .getQuantity()
                                    )
                            );

            OrderItem orderItem =
                    new OrderItem();

            orderItem.setProduct(product);
            orderItem.setQuantity(
                    itemRequest.getQuantity()
            );
            orderItem.setUnitPrice(
                    product.getPrice()
            );

            order.addItem(orderItem);
            items.add(orderItem);

            totalAmount =
                    totalAmount.add(itemTotal);

            productIds.add(product.getId());
            quantities.add(
                    itemRequest.getQuantity()
            );
        }

        if (request.getCouponCode() != null
                && !request.getCouponCode().isBlank()) {

            Coupon coupon =
                    couponService.getValidCoupon(
                            request.getCouponCode()
                    );

            totalAmount =
                    couponService.applyDiscount(
                            totalAmount,
                            coupon
                    );

            Map<String, Object> couponDetails =
                    new HashMap<>();

            couponDetails.put(
                    "couponCode",
                    coupon.getCode()
            );

            couponDetails.put(
                    "discountPercent",
                    coupon.getDiscountPercent()
            );

            couponDetails.put(
                    "discountedTotal",
                    totalAmount
            );

            auditService.log(
                    "Coupon",
                    "COUPON_APPLIED",
                    couponDetails
            );
        }

        for (int i = 0;
             i < productIds.size();
             i++) {

            productService.adjustStock(
                    productIds.get(i),
                    -quantities.get(i)
            );
        }

        order.setTotalAmount(
                totalAmount
        );

        order.setItems(items);

        Order savedOrder =
                orderRepository.save(order);

        List<OrderCreatedItem> eventItems =
                savedOrder.getItems()
                        .stream()
                        .map(item ->
                                new OrderCreatedItem(
                                        item.getProduct().getId(),
                                        item.getProduct().getName(),
                                        item.getQuantity(),
                                        item.getUnitPrice()
                                )
                        )
                        .toList();

        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        savedOrder.getId(),
                        user.getEmail(),
                        eventItems
                );

        orderCreatedEventPublisher.publish(event);

        Map<String, Object> orderDetails =
                new HashMap<>();

        orderDetails.put(
                "orderId",
                savedOrder.getId()
        );

        orderDetails.put(
                "userId",
                user.getId()
        );

        orderDetails.put(
                "email",
                user.getEmail()
        );

        orderDetails.put(
                "status",
                savedOrder.getStatus()
        );

        orderDetails.put(
                "totalAmount",
                savedOrder.getTotalAmount()
        );

        orderDetails.put(
                "itemCount",
                savedOrder.getItems().size()
        );

        auditService.log(
                "Order",
                "ORDER_CREATED",
                orderDetails
        );

        if (TransactionSynchronizationManager
                .isSynchronizationActive()) {

            final BigDecimal revenue =
                    savedOrder.getTotalAmount();

            TransactionSynchronizationManager
                    .registerSynchronization(
                            new TransactionSynchronization() {

                                @Override
                                public void afterCommit() {
                                    revenueCounter.increment(
                                            revenue.doubleValue()
                                    );
                                }
                            }
                    );

        } else {

            revenueCounter.increment(
                    savedOrder
                            .getTotalAmount()
                            .doubleValue()
            );
        }

        return savedOrder;
    }

    public Order getById(Long id) {

        return orderRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found"
                        )
                );
    }

    public Order getByIdForUser(
            Long id,
            String email) {

        Order order = getById(id);

        if (!order.getUser()
                .getEmail()
                .equals(email)) {

            throw new ResourceNotFoundException(
                    "Order not found"
            );
        }

        return order;
    }

    public List<Order> getByUserId(
            Long userId) {

        if (!userRepository
                .existsById(userId)) {

            throw new ResourceNotFoundException(
                    "User not found"
            );
        }

        return orderRepository
                .findByUserId(userId);
    }

    public List<Order> getMyOrders(
            String email) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        return orderRepository
                .findByUserId(
                        user.getId()
                );
    }

    @Transactional
    public Order updateStatus(
            Long orderId,
            OrderStatus newStatus) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        OrderStatus oldStatus =
                order.getStatus();

        if (oldStatus == OrderStatus.PENDING
                && newStatus == OrderStatus.PAID) {

            order.setStatus(
                    OrderStatus.PAID
            );

        } else if (oldStatus == OrderStatus.PAID
                && newStatus == OrderStatus.SHIPPED) {

            order.setStatus(
                    OrderStatus.SHIPPED
            );

        } else if ((oldStatus == OrderStatus.PENDING
                || oldStatus == OrderStatus.PAID)
                && newStatus == OrderStatus.CANCELLED) {

            restoreStock(order);

            order.setStatus(
                    OrderStatus.CANCELLED
            );

        } else {

            throw new InvalidStateTransitionException(
                    "Invalid status transition from "
                            + oldStatus
                            + " to "
                            + newStatus
            );
        }

        Order updatedOrder =
                orderRepository.save(order);

        Map<String, Object> details =
                new HashMap<>();

        details.put(
                "orderId",
                updatedOrder.getId()
        );

        details.put(
                "oldStatus",
                oldStatus
        );

        details.put(
                "newStatus",
                newStatus
        );

        auditService.log(
                "Order",
                "STATUS_UPDATE",
                details
        );

        return updatedOrder;
    }

    private void restoreStock(
            Order order) {

        for (OrderItem item :
                order.getItems()) {

            productService.adjustStock(
                    item.getProduct().getId(),
                    item.getQuantity()
            );
        }
    }
}