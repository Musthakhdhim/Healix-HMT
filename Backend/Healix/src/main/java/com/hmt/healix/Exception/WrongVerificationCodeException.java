package com.hmt.healix.Exception;

public class WrongVerificationCodeException extends RuntimeException{
    public WrongVerificationCodeException(String message){
        super(message);
    }
}
