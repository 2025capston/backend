package com.capston.matching_app.repository;

import com.capston.matching_app.entity.FaceData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FaceDataRepository extends JpaRepository<FaceData, Integer> {

    // userId 기준으로 조회 (유저당 1행만 쓰는 전략이라면 유용)
    Optional<FaceData> findByUserId(Integer userId);

    boolean existsByUserId(Integer userId);

    // 필요시: 특정 유저의 임베딩 삭제
    void deleteByUserId(Integer userId);
}
