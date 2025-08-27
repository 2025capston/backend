package com.capston.matching_app.repository;

import com.capston.matching_app.entity.MatchMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MatchMissionRepository extends JpaRepository<MatchMission, Long> {
    Optional<MatchMission> findByMatchRequestIdAndMissionDate(Long matchRequestId, LocalDate missionDate);

}
