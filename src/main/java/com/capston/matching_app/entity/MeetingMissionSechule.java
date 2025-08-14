package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_mission_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingMissionSechule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_request_id", nullable = false)
    private MatchRequest matchRequest;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheuledTime;

    @Column(name = "is_triggered", nullable = false)
    private boolean triggered = false; //미션 생성여부 체크
}
