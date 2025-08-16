package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class MatchSelection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private MatchRequest matchRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private MatchDateOption selectedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private MatchTimeOption selectedTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private MatchPlaceOption selectedPlace;

    public MatchSelection(MatchRequest matchRequest, MatchDateOption date, MatchTimeOption time, MatchPlaceOption place){
        this.matchRequest = matchRequest;
        this.selectedDate = date;
        this.selectedTime = time;
        this.selectedPlace = place;
    }
}
