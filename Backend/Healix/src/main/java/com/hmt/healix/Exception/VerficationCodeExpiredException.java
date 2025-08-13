package com.hmt.healix.Exception;

public class VerficationCodeExpiredException extends RuntimeException{
    public VerficationCodeExpiredException(String message) {
        super(message);
    }
}
