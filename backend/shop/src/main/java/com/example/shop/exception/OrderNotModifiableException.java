package com.example.shop.exception;

public class OrderNotModifiableException extends RuntimeException {

    public OrderNotModifiableException(String message){
        super(message);
    }

}
