package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "facenet_data")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class FacenetData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Lob
    @Column(name = "embedding_front", nullable = false, columnDefinition = "TEXT")
    private String embeddingFront;

    @Column(name = "created_at", insertable = false, updatable = false)
    private java.sql.Timestamp createdAt;
}
