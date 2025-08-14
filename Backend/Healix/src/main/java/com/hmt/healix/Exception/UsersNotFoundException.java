package com.hmt.healix.Exception;

public class UsersNotFoundException extends RuntimeException{
    public UsersNotFoundException(String message) {
        super(message);
    }
}
