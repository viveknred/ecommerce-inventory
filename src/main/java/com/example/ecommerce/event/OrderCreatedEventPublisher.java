package com.example.ecommerce.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public OrderCreatedEventPublisher(
            ApplicationEventPublisher eventPublisher) {

        this.eventPublisher = eventPublisher;
    }

    public void publish(
            OrderCreatedEvent event) {

        eventPublisher.publishEvent(event);
    }
}