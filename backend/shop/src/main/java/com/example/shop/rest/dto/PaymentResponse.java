package com.example.shop.rest.dto;

import com.example.shop.domain.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    private Long id;
    private BigDecimal amount;
    private LocalDateTime date;
    private Long customerId;

    public static PaymentResponse from(Payment payment) {
        PaymentResponse r = new PaymentResponse();
        r.id = payment.getId();
        r.amount = payment.getAmount();
        r.date = payment.getDate();
        r.customerId = payment.getCustomerId();
        return r;
    }

    public Long getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getDate() { return date; }
    public Long getCustomerId() { return customerId; }
}
