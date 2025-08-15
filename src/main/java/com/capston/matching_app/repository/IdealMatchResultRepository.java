package com.capston.matching_app.repository;

import com.capston.matching_app.entity.IdealMatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IdealMatchResultRepository extends JpaRepository<IdealMatchResult, Integer> {
    List<IdealMatchResult> findByOwnerUserIdOrderByRankOrderAsc(Integer ownerUserId);
    long deleteByOwnerUserId(Integer ownerUserId);
}
