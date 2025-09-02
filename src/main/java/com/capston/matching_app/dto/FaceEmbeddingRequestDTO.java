package com.capston.matching_app.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class FaceEmbeddingRequestDTO {
    @NotNull
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
