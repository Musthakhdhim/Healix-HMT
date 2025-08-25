package com.hmt.healix.Exception;

public class SlotExpiredException extends RuntimeException{
    public SlotExpiredException(String message){
        super(message);
    }
}
