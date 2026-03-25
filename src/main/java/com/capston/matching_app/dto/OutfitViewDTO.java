package com.capston.matching_app.dto;

import com.capston.matching_app.entity.OutfitVisibilityStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class OutfitViewDTO {
    private Integer matchRequestId;
    private LocalDate date;
    private OutfitVisibilityStatus status;
    private String myPhotoUrl;
    private String partnerPhotoUrl; // READY 아니면 null
}