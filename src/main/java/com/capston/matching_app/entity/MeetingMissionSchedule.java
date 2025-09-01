package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

//이건 약속시간 -3시간 시각 저장하는 엔터티

@Entity
@Table(name = "meeting_mission_schedule", indexes = {
        @Index(name="idx_schedule_due", columnList = "scheduled_time, triggered")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingMissionSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@ManyToOne(fetch = FetchType.LAZY)
    @OneToOne(fetch = FetchType.LAZY) // 한 매칭당 1개 스케줄 가정
    @JoinColumn(name = "match_request_id", nullable = false)
    private MatchRequest matchRequest;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime; // meeting_at - 3h (서버TZ)

    @Column(name = "is_triggered", nullable = false)
    private boolean triggered = false; //미션 생성여부 체크

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}
