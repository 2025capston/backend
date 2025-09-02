package com.capston.matching_app.dto;

import com.capston.matching_app.entity.Subregion;

public record SubregionDto(Long id, String nameKo) {
    public static SubregionDto from(Subregion s) {
        return new SubregionDto(s.getId(), s.getNameKo());
    }
}
