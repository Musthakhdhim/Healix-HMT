package com.hmt.healix.Dtos;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDoctorDto {
    private String doctorName;
    private String specialization;
    private String qualification;
    private int experience;
    private String gender;

    private String phoneNumber;
    private String address;
    private int consultingFee;
}
