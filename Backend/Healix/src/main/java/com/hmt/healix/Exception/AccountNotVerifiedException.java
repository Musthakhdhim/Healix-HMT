package com.hmt.healix.Exception;

public class AccountNotVerifiedException extends RuntimeException{
    public AccountNotVerifiedException(String message){
        super(message);
    }
}
