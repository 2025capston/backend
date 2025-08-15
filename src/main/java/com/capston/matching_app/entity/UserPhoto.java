package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_photo")
@Getter @Setter
public class UserPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="user_id", nullable=false)
    private Integer userId;

    @Column(name="photo_url", nullable=false, length=255)
    private String photoUrl;

    @Column(name="is_profile", nullable=false)
    private Boolean isProfile = false;

    @CreationTimestamp
    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    // profile_key는 생성 컬럼이라 매핑 불필요
}
