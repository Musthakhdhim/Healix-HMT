package com.hmt.healix.exception;

public class UserIsNotDoctorException extends RuntimeException{
    public UserIsNotDoctorException(String message){
        super(message);
    }
}
