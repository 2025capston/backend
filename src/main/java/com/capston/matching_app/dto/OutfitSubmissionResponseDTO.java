package com.capston.matching_app.dto;

import com.capston.matching_app.entity.OutfitSubmission;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class OutfitSubmissionResponseDTO {
    private Integer id;
    private Integer matchRequestId;
    private Integer userId;
    private String imageUrl;
    private LocalDate submissionDate;
    private LocalDateTime submittedAt;

    public static OutfitSubmissionResponseDTO from(OutfitSubmission e) {
        return new OutfitSubmissionResponseDTO(
                e.getId() != null ? e.getId().intValue() : null,
                e.getMatchRequest() != null ? Math.toIntExact(e.getMatchRequest().getId()) : null,
                e.getUser() != null ? e.getUser().getUserId().intValue() : null,
                e.getImageUrl(),
                e.getSubmissionDate(),
                e.getSubmittedAt()
        );
    }
}
