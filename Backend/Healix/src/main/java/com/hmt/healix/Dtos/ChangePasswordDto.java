package com.hmt.healix.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDto {

    private String oldPassword;
    @NotEmpty(message = "password can't be empty")
    @NotBlank(message = "password can't be blank")
    @Size(min=8,message = "password should be of minimum 8 characters")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
            message = "Password must be at least 8 characters long and include at least one uppercase letter, one digit, and one special character."
    )
    private String newPassword;
    private String confirmPassword;
}
