package com.capston.matching_app.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class FaceEmbeddingRequestDTO {
    // 클라이언트에서 보내지 않음(컨트롤러에서 토큰 값 주입)
    private Integer userId;

    @NotNull
    private List<Float> embeddingFront;

    @NotNull
    private List<Float> embeddingLeft;

    @NotNull
    private List<Float> embeddingRight;

    @NotNull
    private List<Float> facenetFront;
}
