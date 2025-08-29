package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name ="outfit_submission", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"match_request_id","user_id","submission_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutfitSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //어떤 매칭에서 제출한 건지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_request_id", nullable = false)
    private MatchRequest matchRequest;

    //제출한 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //착장 사진 URL
    @Column(nullable = false)
    private String imageUrl;

    //제출 날짜 (유니크 조건에 포함 -> 하루 1회 제한)
    @Column(name = "submission_date", nullable = false)
    private LocalDate submissionDate;

    @Column(name = "submission_at", nullable = false)
    private LocalDateTime submittedAt;

}
