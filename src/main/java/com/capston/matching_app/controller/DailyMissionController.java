package com.capston.matching_app.controller;

import com.capston.matching_app.entity.DailyMission;
import com.capston.matching_app.service.DailyMissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/daily-missions")
@RequiredArgsConstructor
public class DailyMissionController {

    private final DailyMissionService dailyMissionService;
    
    @PostMapping
    public ResponseEntity<DailyMission> registerMission(@RequestParam String content){
        DailyMission mission = dailyMissionService.registerMission(content);
        return ResponseEntity.ok(mission);
    }
}
