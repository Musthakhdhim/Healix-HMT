package com.hmt.healix.exception;

public class AlreadyVerifiedException extends RuntimeException{
    public AlreadyVerifiedException(String message){
        super(message);
    }
}
