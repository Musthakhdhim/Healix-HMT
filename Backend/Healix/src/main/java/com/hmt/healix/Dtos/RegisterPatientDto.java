package com.hmt.healix.Dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class RegisterPatientDto {

    @NotBlank(message = "name can't be blank")
    @NotEmpty(message = "name can't be empty")
    @Size(min = 5, message = "name should be at least 5  characters")
    private String fullName;

    @NotBlank(message = "address can't be blank")
    @NotEmpty(message = "address can't be empty")
    private String address;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @NotBlank(message = "gender can't be blank")
    @NotEmpty(message = "gender can't be empty")
    private String gender;

    @NotBlank(message = "phone number can't be blank")
    @NotEmpty(message = "phone number can't be empty")
    private String phonenumber;
}
