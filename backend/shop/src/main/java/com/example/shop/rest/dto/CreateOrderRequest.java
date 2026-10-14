package com.example.shop.rest.dto;

public class CreateOrderRequest {
    private Long customerId;
    public CreateOrderRequest() {}
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
}
