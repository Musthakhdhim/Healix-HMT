package com.hmt.healix.Exception;

public class AlreadyVerifiedException extends RuntimeException{
    public AlreadyVerifiedException(String message){
        super(message);
    }
}
