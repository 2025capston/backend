package com.capston.matching_app.repository;

import com.capston.matching_app.entity.MeetingMissionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetingMissionScheduleRepository extends JpaRepository<MeetingMissionSchedule, Long> {

    Optional<MeetingMissionSchedule> findByMatchRequest_Id(Long matchRequestId);

    // 스케줄러가 "예약 시간 <= 현재 시간"이면서 아직 실행 안 된 것만 찾을 때 사용
    List<MeetingMissionSchedule> findByScheduledTimeBeforeAndTriggeredFalse(LocalDateTime time);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update MeetingMissionSchedule s set s.triggered = true where s.id = :id and s.triggered = false")
    int markTriggered(@Param("id") Long id);
}