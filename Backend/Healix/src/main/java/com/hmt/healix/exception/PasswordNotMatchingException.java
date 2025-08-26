package com.hmt.healix.exception;

public class PasswordNotMatchingException extends RuntimeException{
    public PasswordNotMatchingException(String message){
        super(message);
    }
}
