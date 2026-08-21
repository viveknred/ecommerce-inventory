package com.example.ecommerce.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import com.example.ecommerce.config.KafkaConfig;

@Component
public class OrderCreatedKafkaListener {

    private final ObjectMapper objectMapper;

    public OrderCreatedKafkaListener(
            ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaConfig.ORDER_CREATED_TOPIC,
            groupId = "ecommerce-order-events"
    )
    public void handle(String message) {

        try {

            OrderCreatedEvent event =
                    objectMapper.readValue(
                            message,
                            OrderCreatedEvent.class
                    );

            System.out.println(
                    "Started asynchronous processing "
                            + "for OrderCreatedEvent. "
                            + "Order ID: "
                            + event.getOrderId()
            );

            System.out.println(
                    "Simulating receipt and notification "
                            + "generation for "
                            + event.getUserEmail()
            );

            Thread.sleep(2000);

            System.out.println(
                    "Receipt/notification processing completed "
                            + "for order ID: "
                            + event.getOrderId()
            );

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Order event processing interrupted",
                    ex
            );

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to process OrderCreatedEvent",
                    ex
            );
        }
    }
}