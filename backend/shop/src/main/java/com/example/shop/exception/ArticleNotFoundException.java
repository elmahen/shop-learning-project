package com.example.shop.exception;

public class ArticleNotFoundException extends RuntimeException{
    
    public ArticleNotFoundException(String message){
        super(message);
    }

}
