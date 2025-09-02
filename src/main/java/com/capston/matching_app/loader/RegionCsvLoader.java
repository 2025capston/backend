package com.capston.matching_app.loader;

import com.capston.matching_app.entity.Region;
import com.capston.matching_app.entity.Subregion;
import com.capston.matching_app.repository.RegionRepository;
import com.capston.matching_app.repository.SubregionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 앱 시작 시 classpath:/data/regions.csv 를 읽어
 * regions, subregions 테이블에 적재하는 로더.
 * - CSV 헤더: region_name, subregion_name
 * - 재실행 안전: subregions 테이블에 데이터가 있으면 자동 스킵
 */
@Component
@RequiredArgsConstructor
public class RegionCsvLoader implements CommandLineRunner {

    private final RegionRepository regionRepository;
    private final SubregionRepository subregionRepository;

    @Override
    public void run(String... args) throws Exception {
        // 이미 적재되어 있으면 스킵 (재실행 안전)
        if (subregionRepository.count() > 0) return;

        var resource = new ClassPathResource("data/regions.csv");
        try (var in = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
             var parser = new CSVParser(in, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {

            // region_name → Region 캐시 (DB 중복 조회 방지)
            Map<String, Region> regionCache = new HashMap<>();

            for (CSVRecord rec : parser) {
                String regionName = rec.get("region_name");
                String subName    = rec.get("subregion_name");

                // Region upsert (캐시 → DB 조회 → 없으면 저장)
                Region region = regionCache.computeIfAbsent(regionName, rn ->
                        regionRepository.findByNameKo(rn).orElseGet(() -> {
                            Region r = new Region();
                            r.setNameKo(rn);
                            return regionRepository.save(r);
                        })
                );

                // Subregion upsert (region_id + name_ko 유니크 보장)
                if (!subregionRepository.existsByRegionIdAndNameKo(region.getId(), subName)) {
                    Subregion s = new Subregion();
                    s.setRegion(region);
                    s.setNameKo(subName);
                    subregionRepository.save(s);
                }
            }
        }
    }
}
