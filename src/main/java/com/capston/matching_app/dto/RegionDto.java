package com.capston.matching_app.dto;

import com.capston.matching_app.entity.Region;

public record RegionDto(Long id, String nameKo) {
    public static RegionDto from(Region r) {
        return new RegionDto(r.getId(), r.getNameKo());
    }
}
