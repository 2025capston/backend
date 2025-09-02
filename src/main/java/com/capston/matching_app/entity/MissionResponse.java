package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="mission_response", uniqueConstraints = @UniqueConstraint(columnNames = {"match_mission_id","user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    //미션에 대한 응답 내용 -> 긴 문장 허용함
    @Column(nullable = false,columnDefinition = "TEXT")
    private String answer;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @ManyToOne
    private MatchMission matchMission;
}
