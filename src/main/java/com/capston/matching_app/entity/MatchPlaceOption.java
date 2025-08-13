package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class MatchPlaceOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="request_id", nullable = false)
    private MatchRequest matchRequest;

    @Column(nullable = false)
    private String place;

    public MatchPlaceOption(MatchRequest matchRequest, String place) {
        this.matchRequest = matchRequest;
        this.place = place;
    }
}
