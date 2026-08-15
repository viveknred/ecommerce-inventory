package com.example.ecommerce.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.audit.AuditAction;
import com.example.ecommerce.dto.OrderItemRequest;
import com.example.ecommerce.dto.OrderRequest;
import com.example.ecommerce.entity.Coupon;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.InvalidStateTransitionException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CouponService couponService;

    public OrderService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            ProductService productService,
            CouponService couponService) {

        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.couponService = couponService;
    }

    @Transactional
    public Order create(OrderRequest request) {

        User user = userRepository.findById(
                request.getUserId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "User not found"
                ));

        return createOrder(request, user);
    }

    @Transactional
    public Order createForAuthenticatedUser(
            OrderRequest request,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        return createOrder(request, user);
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
                                    ));

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

            productIds.add(
                    product.getId()
            );

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

        return orderRepository.save(order);
    }

    public Order getById(Long id) {

        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found"
                        ));
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
                                ));

        return orderRepository
                .findByUserId(user.getId());
    }

    @Transactional
    @AuditAction(
            entity = "Order",
            action = "STATUS_UPDATE")
    public Order updateStatus(
            Long orderId,
            OrderStatus newStatus) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found"
                                ));

        OrderStatus currentStatus =
                order.getStatus();

        if (currentStatus == OrderStatus.PENDING
                && newStatus == OrderStatus.PAID) {

            order.setStatus(
                    OrderStatus.PAID
            );

        } else if (currentStatus == OrderStatus.PAID
                && newStatus == OrderStatus.SHIPPED) {

            order.setStatus(
                    OrderStatus.SHIPPED
            );

        } else if ((currentStatus == OrderStatus.PENDING
                || currentStatus == OrderStatus.PAID)
                && newStatus == OrderStatus.CANCELLED) {

            restoreStock(order);

            order.setStatus(
                    OrderStatus.CANCELLED
            );

        } else {

            throw new InvalidStateTransitionException(
                    "Invalid status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }

        return orderRepository.save(order);
    }

    private void restoreStock(Order order) {

        for (OrderItem item
                : order.getItems()) {

            productService.adjustStock(
                    item.getProduct().getId(),
                    item.getQuantity()
            );
        }
    }
}