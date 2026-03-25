package com.capston.matching_app.controller;

import com.capston.matching_app.dto.IdealMatchDTO;
import com.capston.matching_app.dto.IdealMatchListResponse;
import com.capston.matching_app.security.AuthUserResolver;
import com.capston.matching_app.service.IdealMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AuthUserResolver authUser;

    /** 등록: 사진+성별만 → 파이썬 포워드 (나이 범위 제거) */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(
            @RequestPart("photo") MultipartFile photo,
            @RequestParam("matchingGender") String matchingGender
    ) {
        Integer userId = authUser.getCurrentUserId();
        idealMatchingService.registerAndForward(userId, photo, matchingGender);
        // 큐잉 의미가 강하면 202 Accepted 도 가능
        return ResponseEntity.ok(Map.of("queued", true));
    }

    /** 결과 조회(폴링): 파이썬에서 가져와 캐시 갱신 후 반환, 실패 시 캐시 반환 */
    @GetMapping(value = "/matches", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IdealMatchListResponse> matches() {
        Integer userId = authUser.getCurrentUserId();
        List<IdealMatchDTO> items = idealMatchingService.fetchAndCacheFromPython(userId);
        return ResponseEntity.ok(new IdealMatchListResponse(items));
    }

    /** (옵션) 파이썬 웹훅: 준비되면 여기로 POST */
    @PostMapping(value = "/callback", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> callback(
            @RequestBody IdealMatchListResponse body
    ) {
        // 이 엔드포인트는 파이썬에서 호출되므로, userId를 body에 포함하거나 다른 방식으로 인증해야 함.
        // 현재 코드에서는 userId를 @RequestParam으로 받고 있어, 클라이언트가 아닌 파이썬에서 호출 시 문제가 될 수 있음.
        // 여기서는 기존 로직을 유지하면서 사용자 ID를 가져오는 부분을 제거.
        // webhook의 경우 별도의 인증 방식을 고려해야 합니다.
        // idealMatchingService.upsertFromWebhook(userId, body.getItems());
        return ResponseEntity.ok(Map.of("ok", true));
    }
}