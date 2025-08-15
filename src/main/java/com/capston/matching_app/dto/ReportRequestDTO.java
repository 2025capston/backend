package com.capston.matching_app.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequestDTO {
    private Long requestId; //신고 대상 매칭 id
    private Long reporterUserId; //신고자 id
    private String reason;
    private String details;
}
