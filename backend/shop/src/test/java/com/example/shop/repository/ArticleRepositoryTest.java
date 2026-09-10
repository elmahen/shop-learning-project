package com.example.shop.repository;

import com.example.shop.domain.Article;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;



@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM order_positions");
        jdbcTemplate.execute("DELETE FROM article");
    }

    @Test
    void shouldSaveAndFindArticle() {
        Article article = new Article("Book", new BigDecimal("5.00"));
        Article saved = articleRepository.save(article);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getArticleName()).isEqualTo("Book");
        assertThat(saved.getPrice()).isEqualTo(new BigDecimal("5.00"));
    }

    @Test
    void shouldFindAllArticles() {
        articleRepository.save(new Article("Book", new BigDecimal("5.00")));
        articleRepository.save(new Article("Pen", new BigDecimal("1.50")));

        var articles = articleRepository.findAll();

        assertThat(articles).hasSize(2);
    }

    @Test
    void shouldDeleteArticle() {
        Article saved = articleRepository.save(new Article("Book", new BigDecimal("5.00")));

        articleRepository.deleteById(saved.getId());

        assertThat(articleRepository.findById(saved.getId())).isEmpty();
    }
}
