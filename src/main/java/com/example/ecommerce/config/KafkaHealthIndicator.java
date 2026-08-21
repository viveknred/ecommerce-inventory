package com.example.ecommerce.config;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

@Component("kafkaHealthIndicator")
public class KafkaHealthIndicator implements HealthIndicator {

    private static final String TOPIC =
            KafkaConfig.ORDER_CREATED_TOPIC;

    private final KafkaAdmin kafkaAdmin;

    public KafkaHealthIndicator(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    @Override
    public Health health() {

        try {

            kafkaAdmin.describeTopics(TOPIC);

            return Health.up()
                    .withDetail(
                            "broker",
                            "localhost:9092"
                    )
                    .withDetail(
                            "topic",
                            TOPIC
                    )
                    .withDetail(
                            "status",
                            "Kafka broker is reachable"
                    )
                    .build();

        } catch (Exception ex) {

            return Health.down()
                    .withDetail(
                            "broker",
                            "localhost:9092"
                    )
                    .withDetail(
                            "topic",
                            TOPIC
                    )
                    .withDetail(
                            "status",
                            "Kafka broker is unavailable"
                    )
                    .withException(ex)
                    .build();
        }
    }
}