package com.capston.matching_app.controller;

import com.capston.matching_app.dto.FaceCompareResultDTO;
import com.capston.matching_app.dto.FaceEmbeddingRequestDTO;
import com.capston.matching_app.dto.FaceEmbeddingResponseDTO;
import com.capston.matching_app.service.FaceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/face")
@Validated
public class FaceController {

    private final FaceService faceService;

    /** 이미지 업로드 → Python /embeddings 호출 → 두 테이블 동시 저장 */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAndEmbed(
            @RequestParam @Min(1) Integer userId,
            @RequestParam("front") MultipartFile front,
            @RequestParam("left")  MultipartFile left,
            @RequestParam("right") MultipartFile right
    ) {
        if (front == null || left == null || right == null ||
                front.isEmpty() || left.isEmpty() || right.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "error", "모든 이미지 파트(front/left/right)가 필요합니다.")
            );
        }
        faceService.upsertEmbeddingsFromImages(userId, front, left, right);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** 임베딩(JSON) 직접 등록: FaceEmbeddingRequestDTO → 두 테이블 동시 저장 */
    @PostMapping(value = "/register-json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerJson(@Valid @RequestBody FaceEmbeddingRequestDTO dto) {
        faceService.saveEmbeddingsFromClient(dto);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** 조회: InsightFace 3개 임베딩 반환 */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getEmbeddings(@PathVariable @Min(1) Integer userId) {
        FaceEmbeddingResponseDTO res = faceService.getEmbeddings(userId);
        return (res == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(res);
    }

    /** 삭제: face_data + facenet_data 함께 삭제 */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteEmbeddings(@PathVariable @Min(1) Integer userId) {
        faceService.deleteByUserId(userId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** 비교 + same=true면 사진 저장 (isProfile 기본 false) */
    @PostMapping(value = "/compare", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FaceCompareResultDTO> compare(
            @RequestParam @Min(1) Integer userId,
            @RequestParam("profile") MultipartFile profile,
            @RequestParam(value = "isProfile", required = false, defaultValue = "false") boolean isProfile
    ) {
        if (profile == null || profile.isEmpty()) {
            return ResponseEntity.badRequest().body(new FaceCompareResultDTO(false));
        }
        boolean same = faceService.compareAndSaveIfMatch(userId, profile, isProfile);
        return ResponseEntity.ok(new FaceCompareResultDTO(same));
    }
}
