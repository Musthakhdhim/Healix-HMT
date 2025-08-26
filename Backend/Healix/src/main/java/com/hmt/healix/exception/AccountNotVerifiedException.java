package com.hmt.healix.exception;

public class AccountNotVerifiedException extends RuntimeException{
    public AccountNotVerifiedException(String message){
        super(message);
    }
}
