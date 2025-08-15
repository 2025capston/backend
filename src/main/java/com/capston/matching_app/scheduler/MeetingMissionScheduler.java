package com.capston.matching_app.scheduler;

import com.capston.matching_app.entity.MeetingMissionSchedule;
import com.capston.matching_app.repository.MeetingMissionScheduleRepository;
import com.capston.matching_app.service.OutfitMissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingMissionScheduler {

    private final MeetingMissionScheduleRepository scheduleRepository;
    private final OutfitMissionService outfitMissionService;

    //1분마다 체크 -> 예약시간 도달하면 OutfitSubmission 미션 생성 및 알림 발송
    @Scheduled(fixedRate = 60000) // 1분마다
    public void checkAndTriggerMissions() {
        LocalDateTime now = LocalDateTime.now();
        List<MeetingMissionSchedule> dueSchedules = scheduleRepository
                .findByScheduledTimeBeforeAndTriggeredFalse(now);

        for (MeetingMissionSchedule schedule : dueSchedules) {
            outfitMissionService.createOutfitMission(schedule.getMatchRequest());
            schedule.setTriggered(true);
            scheduleRepository.save(schedule);
        }
    }
}

