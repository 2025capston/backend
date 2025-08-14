package com.capston.matching_app.repository;

import com.capston.matching_app.entity.DailyMission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyMissionRepository extends JpaRepository<DailyMission,Long> {
}
