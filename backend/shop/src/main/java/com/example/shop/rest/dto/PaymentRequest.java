// PaymentRequest.java
package com.example.shop.rest.dto;

import java.math.BigDecimal;

public class PaymentRequest {
    private Long customerId;
    private BigDecimal amount;
    
    public PaymentRequest() {}
    public Long getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
