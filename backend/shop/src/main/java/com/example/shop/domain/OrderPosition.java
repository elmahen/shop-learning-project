package com.example.shop.domain;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("order_positions")

public class OrderPosition {

    @Id
    private Long id;

    private int quantity;
    private BigDecimal price;
    private Long orderId;
    private Long articleId;

    public OrderPosition() {
    }

    public OrderPosition(int quantity, BigDecimal price, Long orderId, Long articleId) {
        this.quantity = quantity;
        this.price = price;
        this.orderId = orderId;
        this.articleId = articleId;
    }

    public Long getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

}
