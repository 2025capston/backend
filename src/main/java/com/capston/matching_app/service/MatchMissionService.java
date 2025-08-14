package com.capston.matching_app.service;

import com.capston.matching_app.entity.DailyMission;
import com.capston.matching_app.entity.MatchMission;
import com.capston.matching_app.entity.MatchRequest;
import com.capston.matching_app.entity.MatchStatus;
import com.capston.matching_app.repository.DailyMissionRepository;
import com.capston.matching_app.repository.MatchMissionRepository;
import com.capston.matching_app.repository.MatchRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MatchMissionService {

    private final MatchRequestRepository matchRequestRepository;
    private final DailyMissionRepository dailyMissionRepository;
    private final MatchMissionRepository matchMissionRepository;

    public void assignDailyMission() {
        //1.오늘날짜
        LocalDate today = LocalDate.now();

        //2.오늘 미션 랜덤 선택
        List<DailyMission> missions = dailyMissionRepository.findAll();
        if(missions.isEmpty()){
            throw new IllegalStateException("등록된 미션이 없습니다.");
        }
        Random random = new Random();

        //3.오늘 Confirmed 상태의 매칭 가져오기
        List<MatchRequest> confirmedMatches = matchRequestRepository.findByStatus(MatchStatus.CONFIRMED);

        //4.각 매칭에 미션 배정
        for (MatchRequest match : confirmedMatches){
            DailyMission selectedMission = missions.get(random.nextInt(missions.size()));
            MatchMission matchMission = MatchMission.builder()
                    .matchRequest(match)
                    .dailyMission(selectedMission)
                    .missionDate(today)
                    .createdAt(LocalDateTime.now())
                    .build();
            matchMissionRepository.save(matchMission);
        }
    }

}
