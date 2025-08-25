package com.capston.matching_app.repository;

import com.capston.matching_app.entity.Subregion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubregionRepository extends JpaRepository<Subregion, Long> {

    // 특정 시/도의 하위 시·군·구를 가나다순으로 조회
    List<Subregion> findAllByRegionIdAndIsActiveTrueOrderByNameKoAsc(Long regionId);

    // 존재 여부 체크 (CSV 로더에서 중복 방지)
    boolean existsByRegionIdAndNameKo(Long regionId, String nameKo);
}
