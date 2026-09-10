package com.example.shop.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("payment")
public class Payment {

    @Id
    private Long id;
    private BigDecimal amount;
    private LocalDateTime date;
    private Long customerId;

    public Payment() {
    }

    public Payment(BigDecimal amount, Long customerId) {
        this.amount = amount;
        this.customerId = customerId;
        this.date = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

}
