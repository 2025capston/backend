package com.capston.matching_app.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MissionResponseDTO {
    private String myAnswer;
    private String partnerAnswer;
}

