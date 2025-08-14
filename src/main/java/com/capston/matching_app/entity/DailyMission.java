package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="daily_mission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyMission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; //미션내용
}
