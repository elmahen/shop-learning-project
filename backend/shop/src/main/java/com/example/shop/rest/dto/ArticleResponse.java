package com.example.shop.rest.dto;

import com.example.shop.domain.Article;
import java.math.BigDecimal;

public class ArticleResponse {
    private Long id;
    private String articleName;
    private BigDecimal price;

    public static ArticleResponse from(Article article) {
        ArticleResponse r = new ArticleResponse();
        r.id = article.getId();
        r.articleName = article.getArticleName();
        r.price = article.getPrice();
        return r;
    }

    public Long getId() { return id; }
    public String getArticleName() { return articleName; }
    public BigDecimal getPrice() { return price; }
}
