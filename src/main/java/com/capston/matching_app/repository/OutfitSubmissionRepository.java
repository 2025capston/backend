package com.capston.matching_app.repository;

import com.capston.matching_app.entity.MatchRequest;
import com.capston.matching_app.entity.OutfitSubmission;
import com.capston.matching_app.entity.User;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface OutfitSubmissionRepository extends JpaRepository<OutfitSubmission, Long> {
    boolean existsByMatchRequestAndUserAndSubmissionDate(
            MatchRequest matchRequest, User user, LocalDate submissionDate);

}
