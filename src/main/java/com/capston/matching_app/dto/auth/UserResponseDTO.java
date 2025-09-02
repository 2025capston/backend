package com.capston.matching_app.dto.auth;

import lombok.*;


@Getter @AllArgsConstructor
public class UserResponseDTO {
    private Integer id;
    private String name;
    private String email;
    private String phoneNumber;
}
