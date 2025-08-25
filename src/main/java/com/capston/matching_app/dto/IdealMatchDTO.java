package com.capston.matching_app.dto;

import lombok.Data;
import java.util.List;

@Data
public class IdealMatchDTO {

    private Integer userId;                 // matched_user_id
    private List<String> profilePhotos;     // URL 리스트
    private Integer height;
    private Integer rank;                   // 1~10 (rank_order)

    // 🔽 지역: FK + 사람이 읽는 이름 모두 제공
    private Long regionId;                  // 시/도 ID
    private String regionName;              // 시/도 이름
    private Long subregionId;               // 시·군·구 ID
    private String subregionName;           // 시·군·구 이름
}
