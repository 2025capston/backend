package com.capston.matching_app.service;

import com.capston.matching_app.entity.DailyMission;
import com.capston.matching_app.repository.DailyMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DailyMissionService {

    private final DailyMissionRepository dailyMissionRepository;

    public DailyMission registerMission(String content){
        DailyMission mission = DailyMission.builder()
                .content(content)
                .build();
        return dailyMissionRepository.save(mission);
    }
}
