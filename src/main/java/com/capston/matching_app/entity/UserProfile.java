package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.Data;
import jakarta.persistence.OneToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.JoinColumn;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
@Data
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 10)
    private String gender;

    @Column(name = "birth_year")
    private Integer birthYear;

    private Integer height;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String district;

    @Column(name = "sexual_orientation", length = 50)
    private String sexualOrientation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
