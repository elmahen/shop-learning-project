package com.example.shop.exception;

public class OrderPositionNotFoundException extends RuntimeException {

    public OrderPositionNotFoundException(String message){
        super(message);
    }

}
