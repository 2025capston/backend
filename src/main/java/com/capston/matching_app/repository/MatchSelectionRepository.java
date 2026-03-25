package com.capston.matching_app.repository;

import com.capston.matching_app.entity.MatchSelection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchSelectionRepository extends JpaRepository<MatchSelection, Long> {
    Optional<MatchSelection> findByMatchRequest_Id(Long matchRequestId);
}