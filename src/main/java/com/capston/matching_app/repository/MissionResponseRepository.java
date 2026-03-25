package com.capston.matching_app.repository;

import com.capston.matching_app.entity.MissionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MissionResponseRepository extends JpaRepository<MissionResponse,Long> {
    //List<MissionResponse> findByMatchMissionId(Long matchMissionId);
    @Query("select mr from MissionResponse mr " +
            "join fetch mr.user " +
            "join fetch mr.matchMission " +
            "where mr.matchMission.id = :matchMissionId")
    List<MissionResponse> findByMatchMissionId(@Param("matchMissionId") Long matchMissionId);
}
