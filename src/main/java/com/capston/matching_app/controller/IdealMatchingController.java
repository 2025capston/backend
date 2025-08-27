package com.capston.matching_app.controller;

import com.capston.matching_app.dto.IdealMatchDTO;
import com.capston.matching_app.dto.IdealMatchListResponse;
import com.capston.matching_app.service.IdealMatchingService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ideal")
@RequiredArgsConstructor
@Validated
public class IdealMatchingController {

    private final IdealMatchingService idealMatchingService;

    /** 등록: 사진+성별만 → 파이썬 포워드 (나이 범위 제거) */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(
            @RequestParam @NotNull @Min(1) Integer userId,
            @RequestPart("photo") MultipartFile photo,
            @RequestParam("matchingGender") String matchingGender
    ) {
        idealMatchingService.registerAndForward(userId, photo, matchingGender);
        // 큐잉 의미가 강하면 202 Accepted 도 가능
        return ResponseEntity.ok(Map.of("queued", true));
    }

    /** 결과 조회(폴링): 파이썬에서 가져와 캐시 갱신 후 반환, 실패 시 캐시 반환 */
    @GetMapping(value = "/matches", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IdealMatchListResponse> matches(
            @RequestParam @NotNull @Min(1) Integer userId
    ) {
        List<IdealMatchDTO> items = idealMatchingService.fetchAndCacheFromPython(userId);
        return ResponseEntity.ok(new IdealMatchListResponse(items));
    }

    /** (옵션) 파이썬 웹훅: 준비되면 여기로 POST */
    @PostMapping(value = "/callback", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> callback(
            @RequestParam @NotNull @Min(1) Integer userId,
            @RequestBody IdealMatchListResponse body
    ) {
        idealMatchingService.upsertFromWebhook(userId, body.getItems());
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
