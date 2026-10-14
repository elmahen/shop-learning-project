package com.example.shop.rest.dto;

import com.example.shop.domain.Order;
import com.example.shop.domain.OrderStatus;
import java.time.LocalDateTime;

public class OrderResponse {
    private Long id;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private Long customerId;

    public static OrderResponse from(Order order) {
        OrderResponse r = new OrderResponse();
        r.id = order.getId();
        r.status = order.getOrderStatus();
        r.orderDate = order.getOrderDate();
        r.customerId = order.getCustomerId();
        return r;
    }

    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public Long getCustomerId() { return customerId; }
}
