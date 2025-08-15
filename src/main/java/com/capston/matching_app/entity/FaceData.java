package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "face_data")
@Data
public class FaceData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    // TEXT 컬럼 매핑
    @Lob
    @Column(name = "embedding_front", nullable = false, columnDefinition = "TEXT")
    private String embeddingFront;

    @Lob
    @Column(name = "embedding_left", nullable = false, columnDefinition = "TEXT")
    private String embeddingLeft;

    @Lob
    @Column(name = "embedding_right", nullable = false, columnDefinition = "TEXT")
    private String embeddingRight;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
