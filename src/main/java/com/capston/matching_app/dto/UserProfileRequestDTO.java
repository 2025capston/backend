package com.capston.matching_app.dto;

import lombok.Data;

@Data
public class UserProfileRequestDTO {
    private Integer userId;            // users.user_id
    private String gender;             // 성별
    private Integer birthYear;         // 출생연도
    private Integer height;            // 키(cm)
    private String sexualOrientation;  // 성적 지향
    private Long regionId;             // 시/도 FK
    private Long subregionId;          // 시/군/구 FK
}
