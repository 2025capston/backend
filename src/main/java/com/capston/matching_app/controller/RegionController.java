package com.capston.matching_app.controller;

import com.capston.matching_app.dto.RegionDto;
import com.capston.matching_app.dto.SubregionDto;
import com.capston.matching_app.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    // (필요 시 CORS 허용)
    // @CrossOrigin(origins = {"http://10.0.2.2:8080", "http://localhost:3000"})

    @GetMapping("/regions")
    public ResponseEntity<List<RegionDto>> regions() {
        var body = regionService.getRegions();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)).cachePublic())
                .eTag(String.valueOf(body.hashCode()))
                .body(body);
    }

    @GetMapping("/regions/{id}/subregions")
    public ResponseEntity<List<SubregionDto>> subregions(@PathVariable Long id) {
        var body = regionService.getSubregions(id);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)).cachePublic())
                .eTag(String.valueOf(("sub:" + id).hashCode()))
                .body(body);
    }
}
