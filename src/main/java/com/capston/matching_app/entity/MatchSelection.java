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

    // 요청
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private MatchRequest matchRequest;

    // 선택한 날짜 옵션
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "date_option_id", nullable = false)
    private MatchDateOption selectedDate;

    // 선택한 시간 옵션
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_option_id", nullable = false)
    private MatchTimeOption selectedTime;

    // 선택한 장소 옵션
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_option_id", nullable = false)
    private MatchPlaceOption selectedPlace;

    public MatchSelection(MatchRequest matchRequest, MatchDateOption date, MatchTimeOption time, MatchPlaceOption place){
        this.matchRequest = matchRequest;
        this.selectedDate = date;
        this.selectedTime = time;
        this.selectedPlace = place;
    }
}
