package com.capston.matching_app.controller;

import com.capston.matching_app.dto.MatchRequestDTO;
import com.capston.matching_app.dto.ScheduleProposalDTO;
import com.capston.matching_app.entity.User;
import com.capston.matching_app.service.MatchRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//Rest API 컨트롤러임을 나타냄 -> Json 형태로 응답
@RequestMapping("/api/match-requests")
@RequiredArgsConstructor
public class MatchRequestController {

    private final MatchRequestService matchRequestService;

    @PostMapping
    public MatchRequestDTO createMatchRequest(@RequestBody MatchRequestDTO dto) {
        //fromUser, toUser를 DB에서 조회해서 넣어야 함 -> 팀원 UserRepository 구현되면 가져오기
        //지금은 임시 ID만 가진 User객체 생성
        User fromUser = User.builder().userId(Math.toIntExact(dto.getFromUserId())).build();
        User toUser = User.builder().userId(Math.toIntExact(dto.getToUserId())).build();

        return matchRequestService.createMatchRequest(dto, fromUser,toUser);
    }

    /**
     *@PostMapping
     * public MatchRequestDTO createMatchRequest(@RequestBody MatchRequestDTO dto) {
     *     User fromUser = userRepository.findById(dto.getFromUserId())
     *             .orElseThrow(() -> new RuntimeException("보낸 사용자 없음"));
     *     User toUser = userRepository.findById(dto.getToUserId())
     *             .orElseThrow(() -> new RuntimeException("받는 사용자 없음"));
     *
     *     return matchRequestService.createMatchRequest(dto, fromUser, toUser);
     * }
     *
     */

    @GetMapping
    public List<MatchRequestDTO> getAllRequests(){
        //서비스 호출하여 모든 매칭 신청 목록 반환
        return matchRequestService.getAllRequests();
    }

    @PostMapping("/{requestId}/propose-schedule")
    public ResponseEntity<Void> proposeSchedule(
            @PathVariable Long requestId,
            @RequestBody ScheduleProposalDTO dto){
        matchRequestService.proposeSchedule(requestId, dto.getDates(), dto.getPlaces(), dto.getTimes());
        return ResponseEntity.ok().build();

    }
    @PostMapping("/{requestId}/confirm-schedule")
    public ResponseEntity<Void> confirmSchedule(
            @PathVariable Long requestId,
            @RequestParam Long dateOptionId,
            @RequestParam Long timeOptionId,
            @RequestParam Long placeOptionId) {
        matchRequestService.confirmSchedule(requestId, dateOptionId, timeOptionId, placeOptionId);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/{requestId}/cancel")
    public ResponseEntity<Void> cancelMatchRequest(
            @PathVariable Long requestId,
            @RequestParam Long userId) { // 로그인 구현되면 @AuthenticationPrincipal로 변경 가능
        matchRequestService.cancelMatchRequest(requestId, userId);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/{requestId}/meeting-result")
    public ResponseEntity<Void> updateMeetingResult(
            @PathVariable Long requestId,
            @RequestParam String result // "SUCCESS" or "FAIL"
    ) {
        matchRequestService.updateMeetingResult(requestId, result);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/keep-status")
    public ResponseEntity<Void> updateMatchKeepStatus(
            @PathVariable Long requestId,
            @RequestParam String status // "KEEP" or "END"
    ) {
        matchRequestService.updateMatchKeepStatus(requestId, status);
        return ResponseEntity.ok().build();
    }





}
