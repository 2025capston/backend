package com.capston.matching_app.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
    private String gender;              // "남성"/"여성"
    private Integer birthYear;          // 1998 등
    private Integer height;             // cm
    private String city;                // 시/도
    private String district;            // 구/군
    private String sexualOrientation;   // 이성애자/동성애자/양성애자
}
