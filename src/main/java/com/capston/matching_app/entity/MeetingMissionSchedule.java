package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

//이건 약속시간 -3시간 시각 저장하는 엔터티
@Entity
@Table(name = "meeting_mission_schedule",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"match_request_id"}) // matchRequest 당 1개만 허용
        },
        indexes = {
                @Index(name = "idx_schedule_due", columnList = "scheduled_time, is_triggered")
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_request_id", nullable = false, unique = true)
    private MatchRequest matchRequest;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime; // meeting_at - 3h

    @Column(name = "is_triggered", nullable = false)
    private boolean triggered = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

