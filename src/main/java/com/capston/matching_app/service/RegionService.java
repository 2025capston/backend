package com.capston.matching_app.service;

import com.capston.matching_app.dto.RegionDto;
import com.capston.matching_app.dto.SubregionDto;
import com.capston.matching_app.repository.RegionRepository;
import com.capston.matching_app.repository.SubregionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;
    private final SubregionRepository subregionRepository;

    @Transactional(readOnly = true)
    public List<RegionDto> getRegions() {
        return regionRepository.findAllByIsActiveTrueOrderByNameKoAsc()
                .stream().map(RegionDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SubregionDto> getSubregions(Long regionId) {
        return subregionRepository.findAllByRegionIdAndIsActiveTrueOrderByNameKoAsc(regionId)
                .stream().map(SubregionDto::from).toList();
    }
}
