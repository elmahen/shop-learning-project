package com.example.shop.repository;

import org.springframework.data.repository.ListCrudRepository;
import com.example.shop.domain.Article;

public interface ArticleRepository extends ListCrudRepository<Article, Long> {

}
