package com.hmt.healix.Exception;

public class JwtSessionTimeoutException extends RuntimeException{
    public JwtSessionTimeoutException(String message){
        super(message);
    }
}
