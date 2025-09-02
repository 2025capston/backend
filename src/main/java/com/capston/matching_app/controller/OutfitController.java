package com.capston.matching_app.controller;

import com.capston.matching_app.dto.OutfitViewDTO;
import com.capston.matching_app.service.OutfitSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/matches/{matchId}/outfits")
@RequiredArgsConstructor
public class OutfitController {

    private final OutfitSubmissionService outfitService;

    // 실제로는 SecurityContext에서 userId를 가져오도록 교체
    private Long currentUserId() {
        return Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    /** 업로드 + 현재 상태 반환 */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OutfitViewDTO> upload(@PathVariable Long matchId,
                                                @RequestPart("file") MultipartFile file) {
        OutfitViewDTO dto = outfitService.submitAndView(matchId, currentUserId(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /** 오늘 또는 특정일 상태 조회 */
    @GetMapping
    public ResponseEntity<OutfitViewDTO> getOfDate(@PathVariable Long matchId,
                                                   @RequestParam(value = "date", required = false) String dateStr) {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate date = (dateStr == null || dateStr.isBlank())
                ? LocalDate.now(zone)
                : LocalDate.parse(dateStr);
        OutfitViewDTO dto = outfitService.viewOfDate(matchId, currentUserId(), date);
        return ResponseEntity.ok(dto);
    }

    /** 히스토리 조회 */
    @GetMapping("/history")
    public ResponseEntity<List<OutfitViewDTO>> history(@PathVariable Long matchId,
                                                       @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(outfitService.history(matchId, currentUserId(), days));
    }
}