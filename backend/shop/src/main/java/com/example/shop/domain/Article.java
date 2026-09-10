package com.example.shop.domain;

import org.springframework.data.annotation.Id;
import java.math.BigDecimal;
import org.springframework.data.relational.core.mapping.Table;

@Table("article")
public class Article {
    @Id
    private Long id;
    private String articleName;
    private BigDecimal price;

    public Article() {
    }

    public Article(String articleName, BigDecimal price) {
        this.articleName = articleName;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public String getArticleName() {
        return articleName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

}
