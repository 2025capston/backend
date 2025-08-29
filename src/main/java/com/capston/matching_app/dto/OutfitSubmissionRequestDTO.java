package com.capston.matching_app.dto;

import lombok.Getter;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class OutfitSubmissionRequestDTO {
    @NotNull
    private Integer matchRequestId;

    @NotNull
    private Integer userId;

    @NotNull
    private MultipartFile file;
}
