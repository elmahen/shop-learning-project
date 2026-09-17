package com.example.shop.exception;

public class CustomerNotFoundException extends RuntimeException {


    public CustomerNotFoundException(String message){
        super(message);
    }
    
}
