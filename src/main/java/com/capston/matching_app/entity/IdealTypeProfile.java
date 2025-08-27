package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ideal_type_profile")
@Getter @Setter
public class IdealTypeProfile {

    @Id
    @Column(name = "user_id")
    private Integer userId;


    // "MALE" | "FEMALE" | "BOTH"
    @Column(name = "matching_gender", nullable = false, length = 10)
    private String matchingGender;
}
