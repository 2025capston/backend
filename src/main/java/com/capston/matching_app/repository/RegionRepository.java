package com.capston.matching_app.repository;

import com.capston.matching_app.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    // 활성화된 시/도를 가나다순으로 조회
    List<Region> findAllByIsActiveTrueOrderByNameKoAsc();

    // 이름으로 단건 조회 (CSV 로더에서 upsert용)
    Optional<Region> findByNameKo(String nameKo);
}
