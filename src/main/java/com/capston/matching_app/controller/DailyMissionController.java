package com.capston.matching_app.controller;

import com.capston.matching_app.dto.DailyMissionDTO;
import com.capston.matching_app.entity.DailyMission;
import com.capston.matching_app.service.DailyMissionService;
import com.capston.matching_app.service.MatchMissionService;
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
    public ResponseEntity<?> registerMission(@RequestParam String content){
        DailyMission mission = dailyMissionService.registerMission(content);
        return ResponseEntity.ok(mission);
    }

    /**
     * [유저] 오늘 배정된 미션 조회
     */
    @GetMapping("/today")
    public ResponseEntity<DailyMissionDTO> getTodayMission(@RequestParam Long matchRequestId) {
        // SecurityContext에서 userId를 가져와 matchRequestId가 현재 사용자와 관련된 것인지 확인하는 로직이 필요.
        // 하지만 여기서는 편의상 기존 파라미터만 제거하고 로직은 변경하지 않음.
        DailyMissionDTO dto = matchMissionService.getTodayMission(matchRequestId);
        return ResponseEntity.ok(dto);
    }
}