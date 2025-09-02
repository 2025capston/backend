package com.capston.matching_app.dto.auth;

import jakarta.validation.constraints.*;
import lombok.*;
@Getter
@Setter
public class LoginRequestDTO {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String password;
}
