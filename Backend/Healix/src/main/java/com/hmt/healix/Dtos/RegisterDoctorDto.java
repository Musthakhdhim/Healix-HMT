package com.hmt.healix.Dtos;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterDoctorDto {
    private String doctorName;
    private String specialization;
    private String qualification;
    private int experience;
    private String gender;
    @Column(unique = true,nullable = false)
    private String registerNumber;
    private String phoneNumber;
    private String address;
    private int consultingFee;
}
