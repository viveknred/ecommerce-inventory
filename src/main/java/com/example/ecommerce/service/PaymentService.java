package com.example.ecommerce.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.dto.PaymentGatewayRequest;
import com.example.ecommerce.dto.PaymentGatewayResponse;
import com.example.ecommerce.dto.PaymentRequest;
import com.example.ecommerce.dto.PaymentResponse;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.entity.PaymentStatus;
import com.example.ecommerce.exception.InvalidStateTransitionException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final AuditService auditService;
    private final OrderNotificationService orderNotificationService;
    private final PaymentGatewayClient paymentGatewayClient;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            AuditService auditService,
            OrderNotificationService orderNotificationService,
            PaymentGatewayClient paymentGatewayClient) {

        this.paymentRepository =
                paymentRepository;

        this.orderRepository =
                orderRepository;

        this.auditService =
                auditService;

        this.orderNotificationService =
                orderNotificationService;

        this.paymentGatewayClient =
                paymentGatewayClient;
    }

    @Transactional
    public PaymentResponse processPayment(
            PaymentRequest request,
            String email) {

        Order order =
                orderRepository
                        .findById(
                                request.getOrderId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        if (!order.getUser()
                .getEmail()
                .equals(email)) {

            throw new ResourceNotFoundException(
                    "Order not found"
            );
        }

        if (order.getStatus()
                != OrderStatus.PENDING) {

            throw new InvalidStateTransitionException(
                    "Payment can only be processed for a PENDING order"
            );
        }

        if (paymentRepository
                .findByOrderId(
                        order.getId()
                )
                .isPresent()) {

            throw new InvalidStateTransitionException(
                    "Payment already exists for this order"
            );
        }

        PaymentGatewayRequest gatewayRequest =
                new PaymentGatewayRequest(
                        order.getId(),
                        order.getTotalAmount()
                );

        PaymentGatewayResponse gatewayResponse =
                paymentGatewayClient.charge(
                        gatewayRequest
                );

        if (!gatewayResponse.success()) {

            Map<String, Object> details =
                    new HashMap<>();

            details.put(
                    "orderId",
                    order.getId()
            );

            details.put(
                    "amount",
                    order.getTotalAmount()
            );

            details.put(
                    "status",
                    "FAILED"
            );

            details.put(
                    "reason",
                    gatewayResponse.message()
            );

            auditService.log(
                    "Payment",
                    "PAYMENT_GATEWAY_UNAVAILABLE",
                    details
            );

            return new PaymentResponse(
                    null,
                    order.getId(),
                    order.getTotalAmount(),
                    PaymentStatus.FAILED,
                    null,
                    LocalDateTime.now()
            );
        }

        Payment payment =
                new Payment();

        payment.setOrder(
                order
        );

        payment.setAmount(
                order.getTotalAmount()
        );

        payment.setStatus(
                PaymentStatus.SUCCESS
        );

        payment.setTransactionId(
                gatewayResponse.transactionId()
        );

        OrderStatus oldStatus =
                order.getStatus();

        order.setStatus(
                OrderStatus.PAID
        );

        orderRepository.save(
                order
        );

        Payment savedPayment =
                paymentRepository.save(
                        payment
                );

        orderNotificationService
                .notifyStatusChange(
                        order.getId(),
                        oldStatus,
                        OrderStatus.PAID
                );

        Map<String, Object> details =
                new HashMap<>();

        details.put(
                "paymentId",
                savedPayment.getId()
        );

        details.put(
                "orderId",
                order.getId()
        );

        details.put(
                "amount",
                savedPayment.getAmount()
        );

        details.put(
                "status",
                savedPayment.getStatus()
        );

        details.put(
                "transactionId",
                savedPayment.getTransactionId()
        );

        auditService.log(
                "Payment",
                "PAYMENT_SUCCESS",
                details
        );

        return toResponse(
                savedPayment
        );
    }

    public PaymentResponse getPaymentByOrderId(
            Long orderId,
            String email) {

        Payment payment =
                paymentRepository
                        .findByOrderId(
                                orderId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Payment not found"
                                )
                        );

        if (!payment.getOrder()
                .getUser()
                .getEmail()
                .equals(email)) {

            throw new ResourceNotFoundException(
                    "Payment not found"
            );
        }

        return toResponse(
                payment
        );
    }

    private PaymentResponse toResponse(
            Payment payment) {

        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getTransactionId(),
                payment.getCreatedAt()
        );
    }
}