package com.capston.matching_app.controller;

import com.capston.matching_app.dto.DailyMissionDTO;
import com.capston.matching_app.dto.MissionResponseDTO;
import com.capston.matching_app.entity.DailyMission;
import com.capston.matching_app.entity.MissionResponse;
import com.capston.matching_app.service.DailyMissionService;
import com.capston.matching_app.service.MatchMissionService;
import com.capston.matching_app.service.MissionResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/daily-missions")
@RequiredArgsConstructor
public class DailyMissionController {

    private final DailyMissionService dailyMissionService;
    private final MatchMissionService matchMissionService;

    /**
     * [관리자] 미션 등록
     */
    @PostMapping
    public ResponseEntity<DailyMission> registerMission(@RequestParam String content){
        DailyMission mission = dailyMissionService.registerMission(content);
        return ResponseEntity.ok(mission);
    }

    /**
     * [유저] 오늘 배정된 미션 조회
     */
    @GetMapping("/today")
    public ResponseEntity<DailyMissionDTO> getTodayMission(@RequestParam Long matchRequestId) {
        DailyMissionDTO dto = matchMissionService.getTodayMission(matchRequestId);
        return ResponseEntity.ok(dto);
    }
}
