package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ideal_match_result")
@Getter @Setter
public class IdealMatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="owner_user_id", nullable=false)
    private Integer ownerUserId;

    @Column(name="matched_user_id", nullable=false)
    private Integer matchedUserId;

    @Lob
    @Column(name="profile_photos", nullable=false)
    private String profilePhotosJson; // JSON 배열 문자열

    private Integer height;

    // 🔽 기존 city(문자열) 제거 → 정규화 FK로 교체
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;             // 시/도

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subregion_id", nullable = false)
    private Subregion subregion;       // 시·군·구

    @Column(name="rank_order", nullable=false)
    private Integer rankOrder;

    @CreationTimestamp
    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;
}
