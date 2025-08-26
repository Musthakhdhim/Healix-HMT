package com.hmt.healix.exception;

public class VerficationCodeExpiredException extends RuntimeException{
    public VerficationCodeExpiredException(String message) {
        super(message);
    }
}
