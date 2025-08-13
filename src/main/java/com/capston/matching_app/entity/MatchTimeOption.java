package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor
public class MatchTimeOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "date_option_id", nullable = false)
    private MatchDateOption dateOption;

    @Column(nullable = false)
    private LocalTime time;

    public MatchTimeOption(MatchDateOption dateOption, LocalTime time) {
        this.dateOption = dateOption;
        this.time = time;
    }
}
