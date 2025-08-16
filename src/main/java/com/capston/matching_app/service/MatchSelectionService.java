package com.capston.matching_app.service;

import com.capston.matching_app.entity.*;
import com.capston.matching_app.repository.*;
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
    private final MatchDateOptionRepository matchDateOptionRepository;
    private final MatchTimeOptionRepository matchTimeOptionRepository;
    private final MatchPlaceOptionRepository matchPlaceOptionRepository;

    @Transactional
    public void confirmMatch(Long requestId, Long dateOptionId, Long timeOptionId, Long placeOptionId) {
        MatchRequest matchRequest = matchRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매칭 요청입니다."));

        MatchDateOption dateOption = matchDateOptionRepository.findById(dateOptionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 날짜 옵션입니다."));
        MatchTimeOption timeOption = matchTimeOptionRepository.findById(timeOptionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시간 옵션입니다."));
        MatchPlaceOption placeOption = matchPlaceOptionRepository.findById(placeOptionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소 옵션입니다."));

        MatchSelection selection = new MatchSelection(matchRequest, dateOption, timeOption, placeOption);
        matchSelectionRepository.save(selection);

        // 약속 시간 가져오기
        LocalDateTime meetingTime = LocalDateTime.of(
                dateOption.getDate(),
                timeOption.getTime()
        );
        LocalDateTime missionTime = meetingTime.minusHours(3);

        MeetingMissionSchedule schedule = MeetingMissionSchedule.builder()
                .matchRequest(matchRequest)
                .scheduledTime(missionTime)
                .build();

        scheduleRepository.save(schedule);
    }

}

