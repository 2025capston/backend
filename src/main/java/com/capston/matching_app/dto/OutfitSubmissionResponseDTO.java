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
    private LocalDateTime createdAt; // 엔티티 createdAt과 매칭

    public static OutfitSubmissionResponse from(OutfitSubmission e) {
        return new OutfitSubmissionResponse(
                e.getId(),
                e.getMatchRequest().getId(),  // MatchRequest PK가 INT라면 Integer
                e.getUser().getId(),
                e.getImageUrl(),
                e.getSubmissionDate(),
                e.getCreatedAt()
        );
    }
}
