package com.capston.matching_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaceEmbeddingResponseDTO {
    private List<Float> embeddingFront;
    private List<Float> embeddingLeft;
    private List<Float> embeddingRight;
}
