package com.capston.matching_app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class OutfitSubmissionRequestDTO {
    @NotNull
    private Long matchRequestId;
    @NotNull private Long userId;
    @NotNull private MultipartFile file;
}
