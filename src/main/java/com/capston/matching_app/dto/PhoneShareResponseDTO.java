package com.capston.matching_app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 응답에서 생략
public class PhoneShareResponseDTO {
    private boolean bothKept;               // 둘 다 KEEP 상태인지
    private boolean bothShared;             // 둘 다 '전송'을 눌렀는지
    private String myPhoneNumber;           // 항상 제공 (내 번호)
    private String partnerPhoneNumber;      // bothShared == true 일 때만 제공
    private LocalDateTime phoneExchangedAt; // (옵션) 둘 다 전송 완료된 시각
}
