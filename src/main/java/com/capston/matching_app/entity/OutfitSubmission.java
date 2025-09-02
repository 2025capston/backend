package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "outfit_submission")
@Getter
@Setter                         // ← 서비스에서 setImageUrl, setSubmittedAt 사용
@Accessors(chain = false)       // 체이닝 안 써도 OK. 써도 무방.
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutfitSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_request_id", nullable = false)
    private MatchRequest matchRequest;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "submission_date", nullable = false)
    private LocalDate submissionDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 서비스에서 setSubmittedAt(...) 사용 → DB 컬럼명 submission_at
    @Column(name = "submission_at", updatable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    void prePersist() {
        if (submittedAt == null) submittedAt = LocalDateTime.now();
        if (createdAt == null)   createdAt   = LocalDateTime.now();
    }
}
