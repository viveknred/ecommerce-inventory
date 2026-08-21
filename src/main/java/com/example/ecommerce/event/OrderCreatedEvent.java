package com.example.ecommerce.event;

import java.util.List;

public class OrderCreatedEvent {

    private Long orderId;
    private String userEmail;
    private List<OrderCreatedItem> items;

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(
            Long orderId,
            String userEmail,
            List<OrderCreatedItem> items) {

        this.orderId = orderId;
        this.userEmail = userEmail;
        this.items = items;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<OrderCreatedItem> getItems() {
        return items;
    }

    public void setItems(List<OrderCreatedItem> items) {
        this.items = items;
    }
}