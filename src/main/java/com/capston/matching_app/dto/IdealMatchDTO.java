package com.capston.matching_app.dto;

import lombok.Data;
import java.util.List;

@Data
public class IdealMatchDTO {
    private Integer userId;              // matched_user_id
    private List<String> profilePhotos;  // URL 리스트
    private Integer height;
    private String city;
    private Integer rank;                // 1~10
}
