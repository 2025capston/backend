package com.capston.matching_app.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyMissionDTO {
    private Long matchMissionId;   // 매칭 미션 ID
    private String content;        // 미션 내용
    private LocalDate missionDate; // 배정 날짜
}

