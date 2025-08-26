package com.hmt.healix.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VerfiyUserDto {
    private String email;
    private String verificationCode;
}
