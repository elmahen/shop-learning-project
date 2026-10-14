package com.example.shop.rest.dto;

public class AddItemRequest {
    private Long articleId;
    private int quantity;
    public AddItemRequest() {}
    public Long getArticleId() { return articleId; }
    public int getQuantity() { return quantity; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
