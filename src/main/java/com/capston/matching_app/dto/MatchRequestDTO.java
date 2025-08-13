package com.capston.matching_app.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchRequestDTO {
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String  status;
    private LocalDateTime requestTime;
    private String meetingResult;
    private String matchKeepStatus;
}
