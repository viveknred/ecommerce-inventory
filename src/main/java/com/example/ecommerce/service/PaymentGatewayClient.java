package com.example.ecommerce.service;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.ecommerce.dto.PaymentGatewayRequest;
import com.example.ecommerce.dto.PaymentGatewayResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class PaymentGatewayClient {

    private final WebClient webClient;

    public PaymentGatewayClient(
            WebClient.Builder webClientBuilder) {

        this.webClient =
                webClientBuilder
                        .baseUrl(
                                "http://localhost:8080"
                        )
                        .build();
    }

    @Retry(
            name = "paymentGateway"
    )
    @CircuitBreaker(
            name = "paymentGateway",
            fallbackMethod = "fallback"
    )
    public PaymentGatewayResponse charge(
            PaymentGatewayRequest request) {

        return webClient
                .post()
                .uri(
                        "/internal/payment-gateway/charge"
                )
                .bodyValue(request)
                .retrieve()
                .bodyToMono(
                        PaymentGatewayResponse.class
                )
                .block(
                        Duration.ofSeconds(5)
                );
    }

    private PaymentGatewayResponse fallback(
            PaymentGatewayRequest request,
            Throwable throwable) {

        System.out.println(
                "[Resilience4j] Payment gateway fallback triggered for order "
                        + request.orderId()
                        + ". Reason: "
                        + throwable.getClass().getSimpleName()
                        + " - "
                        + throwable.getMessage()
        );

        return new PaymentGatewayResponse(
                false,
                null,
                "Payment gateway temporarily unavailable"
        );
    }
}