package com.capston.matching_app.repository;

import com.capston.matching_app.entity.FacenetData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FacenetDataRepository extends JpaRepository<FacenetData, Integer> {
    Optional<FacenetData> findByUserId(Integer userId);
    void deleteByUserId(Integer userId);
}
