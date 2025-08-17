package com.hmt.healix.Exception;

public class UserIsNotDoctorException extends RuntimeException{
    public UserIsNotDoctorException(String message){
        super(message);
    }
}
