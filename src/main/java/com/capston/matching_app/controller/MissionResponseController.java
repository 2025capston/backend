package com.capston.matching_app.controller;

import com.capston.matching_app.dto.MissionResponseDTO;
import com.capston.matching_app.entity.MissionResponse;
import com.capston.matching_app.service.MissionResponseService;
import com.capston.matching_app.service.OutfitSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController //REST API 컨트롤러임을 명시 -> Json응답
@RequestMapping("/mission-response") //API기본 경로 설정
@RequiredArgsConstructor //final 필드 자동 생성자 주입
public class MissionResponseController {

    private final MissionResponseService missionResponseService;
    private final OutfitSubmissionService outfitSubmissionService;

    @PostMapping
    public ResponseEntity<MissionResponse> submitResponse(
            @RequestParam Long matchMissionId, //매칭미션ID
            @RequestParam Long userId, //제출한 유저 id
            @RequestParam String answer //응답 내용
    ){
        //서비스 계층에 요청 위임하여 응답 데이터 생성/저장
        MissionResponse response = missionResponseService.submitResponse(matchMissionId,userId,answer);
        return ResponseEntity.ok(response); //200 ok + 저장된 데이터 반환
    }

    @GetMapping
    public ResponseEntity<MissionResponseDTO> getResponses(
            @RequestParam Long matchMissionId,
            @RequestParam Long userId
    ){
        MissionResponseDTO dto = missionResponseService.getMissionResponses(matchMissionId, userId);
        return ResponseEntity.ok(dto);
    }
}
