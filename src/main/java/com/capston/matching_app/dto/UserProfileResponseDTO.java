package com.capston.matching_app.dto;

import com.capston.matching_app.entity.UserProfile;
import lombok.Data;

@Data
public class UserProfileResponseDTO {
    private Integer userId;
    private String gender;
    private Integer birthYear;
    private Integer height;
    private String sexualOrientation;
    private Long regionId;
    private String regionName;
    private Long subregionId;
    private String subregionName;

    public static UserProfileResponseDTO from(UserProfile up) {
        UserProfileResponseDTO dto = new UserProfileResponseDTO();
        dto.setUserId(up.getUserId());
        dto.setGender(up.getGender());
        dto.setBirthYear(up.getBirthYear());
        dto.setHeight(up.getHeight());
        dto.setSexualOrientation(up.getSexualOrientation());
        dto.setRegionId(up.getRegion().getId());
        dto.setRegionName(up.getRegion().getNameKo());
        dto.setSubregionId(up.getSubregion().getId());
        dto.setSubregionName(up.getSubregion().getNameKo());
        return dto;
    }
}
