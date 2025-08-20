package com.capston.matching_app.dto.auth;

import jakarta.validation.constraints.*;
import lombok.*;


@Getter @Setter
public class RefreshRequestDTO {
    @NotBlank
    private String refreshToken;
}
