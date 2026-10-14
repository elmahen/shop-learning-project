package com.example.shop.rest.dto;

import com.example.shop.domain.OrderPosition;
import java.math.BigDecimal;

public class OrderPositionResponse {
    private Long id;
    private int quantity;
    private BigDecimal price;
    private Long articleId;

    public static OrderPositionResponse from(OrderPosition op) {
        OrderPositionResponse r = new OrderPositionResponse();
        r.id = op.getId();
        r.quantity = op.getQuantity();
        r.price = op.getPrice();
        r.articleId = op.getArticleId();
        return r;
    }

    public Long getId() { return id; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
    public Long getArticleId() { return articleId; }
}
