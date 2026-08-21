package com.example.ecommerce.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            AuditService auditService) {

        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.auditService = auditService;
    }

    @Transactional
    public PaymentResponse processPayment(
            PaymentRequest request,
            String email) {

        Order order = orderRepository
                .findById(request.getOrderId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found"));

        if (!order.getUser()
                .getEmail()
                .equals(email)) {

            throw new ResourceNotFoundException(
                    "Order not found");
        }

        if (order.getStatus()
                != OrderStatus.PENDING) {

            throw new InvalidStateTransitionException(
                    "Payment can only be processed for a PENDING order");
        }

        if (paymentRepository
                .findByOrderId(order.getId())
                .isPresent()) {

            throw new InvalidStateTransitionException(
                    "Payment already exists for this order");
        }

        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(
                "TXN-" + UUID.randomUUID()
        );

        order.setStatus(OrderStatus.PAID);

        orderRepository.save(order);

        Payment savedPayment =
                paymentRepository.save(payment);

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

        return toResponse(savedPayment);
    }

    public PaymentResponse getPaymentByOrderId(
            Long orderId,
            String email) {

        Payment payment = paymentRepository
                .findByOrderId(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment not found"));

        if (!payment.getOrder()
                .getUser()
                .getEmail()
                .equals(email)) {

            throw new ResourceNotFoundException(
                    "Payment not found");
        }

        return toResponse(payment);
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