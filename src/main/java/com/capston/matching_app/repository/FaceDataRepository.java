package com.capston.matching_app.repository;

import com.capston.matching_app.entity.FaceData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FaceDataRepository extends JpaRepository<FaceData, Long> {
    Optional<FaceData> findByUserId(Integer userId);
    boolean existsByUserId(Integer userId);
    void deleteByUserId(Integer userId);
}
