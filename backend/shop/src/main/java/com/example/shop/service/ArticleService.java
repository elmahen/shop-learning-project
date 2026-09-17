package com.example.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.shop.domain.Article;
import com.example.shop.repository.ArticleRepository;

@Service 

public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository){
        this.articleRepository = articleRepository;
    }


    public List<Article> getAllArticles(){
        return articleRepository.findAll();
    }
    
}
