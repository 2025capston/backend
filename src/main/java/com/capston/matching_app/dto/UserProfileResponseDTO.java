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

        var region = up.getRegion();
        if (region != null) {
            dto.setRegionId(region.getId());
            dto.setRegionName(region.getNameKo());
        }

        var sub = up.getSubregion();
        if (sub != null) {
            dto.setSubregionId(sub.getId());
            dto.setSubregionName(sub.getNameKo());
        }
        return dto;
    }
}
