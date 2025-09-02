package com.capston.matching_app.controller;

import com.capston.matching_app.entity.UserPhoto;
import com.capston.matching_app.security.AuthUserResolver;
import com.capston.matching_app.service.UserPhotoService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me/photos")
@RequiredArgsConstructor
@Validated
public class UserPhotoController {

    private final UserPhotoService userPhotoService;
    private final AuthUserResolver authUser; // ★ JWT에서 userId 추출

    /** 업로드 + DB 저장 (isProfile=true면 기존 대표 해제 후 저장) */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isProfile", required = false, defaultValue = "false") boolean isProfile
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "파일이 비었습니다."));
        }
        Integer userId = authUser.getCurrentUserId(); // ★ 토큰 기반
        UserPhoto saved = userPhotoService.upload(userId, file, isProfile);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "photoId", saved.getId(),
                "photoUrl", saved.getPhotoUrl(),
                "isProfile", saved.getIsProfile()
        ));
    }

    /** 내 사진 목록 조회 */
    @GetMapping
    public ResponseEntity<List<UserPhoto>> list() {
        Integer userId = authUser.getCurrentUserId(); // ★ 토큰 기반
        return ResponseEntity.ok(userPhotoService.list(userId));
    }

    /** 대표 사진 변경 (기존 사진 중 하나를 대표로 승격) */
    @PatchMapping("/{photoId}/profile")
    public ResponseEntity<?> setProfile(@PathVariable @Min(1) Integer photoId) {
        Integer userId = authUser.getCurrentUserId(); // ★ 토큰 기반
        userPhotoService.setProfile(userId, photoId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** 사진 삭제 */
    @DeleteMapping("/{photoId}")
    public ResponseEntity<?> delete(@PathVariable @Min(1) Integer photoId) {
        Integer userId = authUser.getCurrentUserId(); // ★ 토큰 기반
        boolean ok = userPhotoService.delete(userId, photoId);
        return ResponseEntity.ok(Map.of("success", ok));
    }
}
