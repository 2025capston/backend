package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="match_request")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="to_user_id", nullable = false)
    private User toUser;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @Column(name="request_time", nullable = false)
    private LocalDateTime requestTime;

    @Enumerated(EnumType.STRING)
    private MeetingResult meetingResult;

    @Enumerated(EnumType.STRING)
    private MatchKeepStatus matchKeepStatus;

    // ====== ★ 전화번호 교환 관련 필드 추가 ======
    @Column(name = "share_phone_from", nullable = false)   // from_user가 '전송' 눌렀는지
    private boolean sharePhoneFrom;                        // ★추가

    @Column(name = "share_phone_to", nullable = false)     // to_user가 '전송' 눌렀는지
    private boolean sharePhoneTo;                          // ★추가

    @Column(name = "phone_exchanged_at")                   // 서로 전송 완료 시각(옵션)
    private LocalDateTime phoneExchangedAt;                // ★추가
}
