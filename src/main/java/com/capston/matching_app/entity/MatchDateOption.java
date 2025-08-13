package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class MatchDateOption {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="request_id", nullable = false)
    private MatchRequest matchRequest;

    @Column(nullable = false)
    private LocalDate date;

    public MatchDateOption(MatchRequest request, LocalDate date) {
        this.matchRequest = request;
        this.date = date;
    }
}
