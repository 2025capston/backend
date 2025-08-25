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
    private String status;
    private LocalDateTime requestTime;
    private String meetingResult;
    private String matchKeepStatus;

    // ===== 전화번호 교환 관련 추가 =====
    private boolean sharePhoneFrom;      // fromUser가 '전송' 눌렀는지
    private boolean sharePhoneTo;        // toUser가 '전송' 눌렀는지
    private LocalDateTime phoneExchangedAt; // 둘 다 전송 완료 시각
}
