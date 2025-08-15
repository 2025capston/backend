package com.capston.matching_app.service;

import com.capston.matching_app.entity.MatchRequest;
import com.capston.matching_app.entity.MatchSelection;
import com.capston.matching_app.entity.MeetingMissionSchedule;
import com.capston.matching_app.repository.MatchRequestRepository;
import com.capston.matching_app.repository.MatchSelectionRepository;
import com.capston.matching_app.repository.MeetingMissionScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class MatchSelectionService {

    private final MeetingMissionScheduleRepository scheduleRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final MatchSelectionRepository matchSelectionRepository;

    @Transactional
    public void confirmMatch(Long requestId, LocalDate selectedDate, LocalTime selectedTime, Long placeId) {
        // MatchSelection 저장 로직...
        MatchRequest matchRequest = matchRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매칭 요청입니다."));

        MatchSelection selection = new MatchSelection();
        selection.setRequest(matchRequest);
        selection.setSelectedDate(selectedDate);
        selection.setSelectedTime(selectedTime);
        selection.setSelectedPlaceId(placeId);
        matchSelectionRepository.save(selection);

        // 약속 시간 -3시간 계산
        LocalDateTime meetingTime = LocalDateTime.of(selectedDate, selectedTime);
        LocalDateTime missionTime = meetingTime.minusHours(3);

        // 예약 저장
        MeetingMissionSchedule schedule = MeetingMissionSchedule.builder()
                .matchRequest(matchRequest)
                .scheduledTime(missionTime)
                .build();

        scheduleRepository.save(schedule);
    }
}

