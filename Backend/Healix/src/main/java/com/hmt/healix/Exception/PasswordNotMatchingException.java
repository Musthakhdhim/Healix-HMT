package com.hmt.healix.Exception;

public class PasswordNotMatchingException extends RuntimeException{
    public PasswordNotMatchingException(String message){
        super(message);
    }
}
