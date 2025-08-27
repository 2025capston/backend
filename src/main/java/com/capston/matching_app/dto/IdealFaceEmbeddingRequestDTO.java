package com.capston.matching_app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class IdealFaceEmbeddingRequestDTO {

    @NotNull
    private Long userId;

    // 이상형은 FaceNet 정면 1개만 사용
    @NotNull
    private List<Float> facenetFront;
}
