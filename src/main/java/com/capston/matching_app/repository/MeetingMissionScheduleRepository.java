package com.capston.matching_app.repository;

import com.capston.matching_app.entity.MeetingMissionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingMissionScheduleRepository extends JpaRepository<MeetingMissionSchedule, Long> {

    // 스케줄러가 "예약 시간 <= 현재 시간"이면서 아직 실행 안 된 것만 찾을 때 사용
    List<MeetingMissionSchedule> findByScheduledTimeBeforeAndTriggeredFalse(LocalDateTime time);
}
