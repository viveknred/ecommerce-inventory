package com.example.ecommerce.event;

import tools.jackson.databind.ObjectMapper;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.ecommerce.config.KafkaConfig;

@Component
public class OrderCreatedKafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderCreatedKafkaPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {

        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void publishAfterCommit(
            OrderCreatedEvent event) {

        try {

            String json =
                    objectMapper.writeValueAsString(
                            event
                    );

            kafkaTemplate.send(
                    KafkaConfig.ORDER_CREATED_TOPIC,
                    String.valueOf(
                            event.getOrderId()
                    ),
                    json
            );

            System.out.println(
                    "Published OrderCreatedEvent to Kafka "
                            + "for order ID: "
                            + event.getOrderId()
            );

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to publish OrderCreatedEvent",
                    ex
            );
        }
    }
}