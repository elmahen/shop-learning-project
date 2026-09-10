package com.example.shop.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("orders")
public class Order {

    @Id
    private Long id;
    private OrderStatus orderStatus;
    private LocalDateTime orderDate;
    private Long customerId;

    public Order() {
    }

    public Order(Long customerId, OrderStatus orderStatus) {
        this.customerId = customerId;
        this.orderStatus = orderStatus;
        this.orderDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    @Override
    public String toString() {
        return "Order [id=" + id + ", orderStatus=" + orderStatus + ", orderDate=" + orderDate + ", customerId="
                + customerId + "]";
    }

}
