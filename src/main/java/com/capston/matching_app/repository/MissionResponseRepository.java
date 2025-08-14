package com.capston.matching_app.repository;

import com.capston.matching_app.entity.MissionResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionResponseRepository extends JpaRepository<MissionResponse,Long> {
    List<MissionResponse> findByMatchMissionId(Long matchMissionId);
}
