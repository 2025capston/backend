package com.capston.matching_app.service;

import com.capston.matching_app.entity.MatchDateOption;
import com.capston.matching_app.entity.MatchSelection;
import com.capston.matching_app.entity.MatchTimeOption;
import com.capston.matching_app.entity.MeetingMissionSchedule;
import com.capston.matching_app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class MeetingMissionScheduleService {

    private final MeetingMissionScheduleRepository scheduleRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final MatchSelectionRepository matchSelectionRepository;

    @Value("${app.timezone:Asia/Seoul}")
    private String appTz;

    @Transactional
    public void upsertScheduleMinus3h(Long matchRequestId) {

        matchRequestRepository.findById(matchRequestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매칭입니다. matchId=" + matchRequestId));

        // 연관관계 기반 탐색
        MatchSelection sel = matchSelectionRepository.findByMatchRequest_Id(matchRequestId)
                .orElseThrow(() -> new IllegalStateException("선택된 일정이 없습니다. matchId=" + matchRequestId));

        // 연관객체에서 바로 날짜/시간 꺼내기
        LocalDate date = sel.getSelectedDate().getDate();
        LocalTime time = sel.getSelectedTime().getTime();

        ZoneId zone = ZoneId.of(appTz);
        LocalDateTime meetingAt = LocalDateTime.of(date, time);
        LocalDateTime scheduled = meetingAt.minusHours(3);

        MeetingMissionSchedule schedule = scheduleRepository.findByMatchRequest_Id(matchRequestId)
                .orElseGet(() -> MeetingMissionSchedule.builder()
                        .matchRequest(matchRequestRepository.getReferenceById(matchRequestId))
                        .triggered(false)
                        .build());

        schedule.setScheduledTime(scheduled);
        schedule.setTriggered(false);

        scheduleRepository.save(schedule);
    }
}


