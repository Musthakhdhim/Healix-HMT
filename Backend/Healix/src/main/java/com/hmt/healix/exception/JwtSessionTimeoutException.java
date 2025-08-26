package com.hmt.healix.exception;

public class JwtSessionTimeoutException extends RuntimeException{
    public JwtSessionTimeoutException(String message){
        super(message);
    }
}
