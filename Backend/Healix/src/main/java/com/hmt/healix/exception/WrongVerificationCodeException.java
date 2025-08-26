package com.hmt.healix.exception;

public class WrongVerificationCodeException extends RuntimeException{
    public WrongVerificationCodeException(String message){
        super(message);
    }
}
