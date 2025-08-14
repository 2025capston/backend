package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="match_mission",
    uniqueConstraints = @UniqueConstraint(columnNames = {"match_request_id","mission_date"}))
//match_request_id 컬럼 값이 유일하도록 설정해서, 하나의 매칭 미션에 대해 한 번만 응답 가능하도록 설정함
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_request_id", nullable = false)
    private MatchRequest matchRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private DailyMission dailyMission;

    @Column(name = "mission_date", nullable = false)
    private LocalDate missionDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
