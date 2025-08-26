package com.hmt.healix.dtos;

import com.hmt.healix.entity.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message="username can't be blank")
    @NotEmpty(message = "username can't be empty")
    @Size(min = 5, max = 15,message = "username should be between 5 to 15 characters")
    private String username;

    @Email(message = "Should be a valid email")
    @NotEmpty(message = "can't be empty")
    private String email;

    @Size(min=8,message = "password should be of minimum 8 characters")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
            message = "Password must be at least 8 characters long and include at least one uppercase letter, one digit, and one special character."
    )
    private String password;
    private Role role;
}
