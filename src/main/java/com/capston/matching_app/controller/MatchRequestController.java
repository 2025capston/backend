package com.capston.matching_app.controller;

import com.capston.matching_app.dto.MatchRequestDTO;
import com.capston.matching_app.dto.PhoneShareResponseDTO;
import com.capston.matching_app.dto.ScheduleProposalDTO;
import com.capston.matching_app.entity.User;
import com.capston.matching_app.service.MatchRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/match-requests")
@RequiredArgsConstructor
public class MatchRequestController {

    private final MatchRequestService matchRequestService;

    @PostMapping
    public MatchRequestDTO createMatchRequest(@RequestBody MatchRequestDTO dto) {
        User fromUser = User.builder().userId(Math.toIntExact(dto.getFromUserId())).build();
        User toUser   = User.builder().userId(Math.toIntExact(dto.getToUserId())).build();
        return matchRequestService.createMatchRequest(dto, fromUser, toUser);
    }

    @GetMapping
    public List<MatchRequestDTO> getAllRequests() {
        return matchRequestService.getAllRequests();
    }

    @PostMapping("/{requestId}/propose-schedule")
    public ResponseEntity<Void> proposeSchedule(
            @PathVariable Long requestId,
            @RequestBody ScheduleProposalDTO dto) {
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
            @RequestParam Long userId) {
        matchRequestService.cancelMatchRequest(requestId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/meeting-result")
    public ResponseEntity<Void> updateMeetingResult(
            @PathVariable Long requestId,
            @RequestParam String result) {
        matchRequestService.updateMeetingResult(requestId, result);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/keep-status")
    public ResponseEntity<Void> updateMatchKeepStatus(
            @PathVariable Long requestId,
            @RequestParam String status) {
        matchRequestService.updateMatchKeepStatus(requestId, status);
        return ResponseEntity.ok().build();
    }

    // ===== 신규: 전화번호 전송/조회 =====

    // 전송/취소 토글
    // 예) POST /api/match-requests/123/share-phone?share=true
    @PostMapping("/{requestId}/share-phone")
    public ResponseEntity<PhoneShareResponseDTO> sharePhone(
            @PathVariable Long requestId,
            @RequestParam boolean share,
            Authentication authentication) {
        String email = authentication.getName(); // JWT subject = email
        PhoneShareResponseDTO res = matchRequestService.toggleSharePhone(requestId, email, share);
        return ResponseEntity.ok(res);
    }

    // 상태 조회(폴링/재진입)
    // 예) GET /api/match-requests/123/phone-status
    @GetMapping("/{requestId}/phone-status")
    public ResponseEntity<PhoneShareResponseDTO> phoneStatus(
            @PathVariable Long requestId,
            Authentication authentication) {
        String email = authentication.getName();
        PhoneShareResponseDTO res = matchRequestService.getPhoneShareStatus(requestId, email);
        return ResponseEntity.ok(res);
    }
}
