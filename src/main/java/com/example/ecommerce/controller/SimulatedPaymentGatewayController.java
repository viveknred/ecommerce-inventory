package com.example.ecommerce.controller;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.ecommerce.dto.PaymentGatewayRequest;
import com.example.ecommerce.dto.PaymentGatewayResponse;

@RestController
@RequestMapping("/internal/payment-gateway")
public class SimulatedPaymentGatewayController {

    private final AtomicBoolean failureMode =
            new AtomicBoolean(false);

    @PostMapping("/charge")
    public PaymentGatewayResponse charge(
            @RequestBody PaymentGatewayRequest request) {

        if (failureMode.get()) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Simulated payment gateway is unavailable"
            );
        }

        return new PaymentGatewayResponse(
                true,
                "TXN-"
                        + UUID.randomUUID(),
                "Payment processed successfully"
        );
    }

    @PostMapping("/failure/{enabled}")
    public String setFailureMode(
            @PathVariable("enabled")
            boolean enabled) {

        failureMode.set(enabled);

        return enabled
                ? "Payment gateway failure mode ENABLED"
                : "Payment gateway failure mode DISABLED";
    }
}