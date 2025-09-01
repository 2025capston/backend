package com.capston.matching_app.repository;

import com.capston.matching_app.entity.MatchRequest;
import com.capston.matching_app.entity.OutfitSubmission;
import com.capston.matching_app.entity.User;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OutfitSubmissionRepository extends JpaRepository<OutfitSubmission, Integer> {

    boolean existsByMatchRequestAndUserAndSubmissionDate(
            MatchRequest matchRequest, User user, LocalDate submissionDate);

    Optional<OutfitSubmission> findByMatchRequestAndUserAndSubmissionDate(
            MatchRequest matchRequest, User user, LocalDate submissionDate);

    @Query("select os from OutfitSubmission os " +
            "where os.matchRequest = :match and os.submissionDate = :date")
    List<OutfitSubmission> findAllByMatchAndDate(@Param("match") MatchRequest match,
                                                 @Param("date") LocalDate date);

    @Query("select os from OutfitSubmission os " +
            "where os.matchRequest = :match and os.user in (:u1, :u2) and os.submissionDate = :date")
    List<OutfitSubmission> findPairOfDay(@Param("match") MatchRequest match,
                                         @Param("u1") User u1,
                                         @Param("u2") User u2,
                                         @Param("date") LocalDate date);
    @Query("select os from OutfitSubmission os " +
            "where os.matchRequest = :match and os.user = :user " +
            "order by os.submissionDate desc")
    List<OutfitSubmission> findHistory(@Param("match") MatchRequest match,
                                       @Param("user") User user);

    /**
    @Query("select os from OutfitSubmission os " +
            "where os.matchRequest = :match and os.user = :user " +
            "order by os.submissionDate desc")
    List<OutfitSubmission> findHistory(@Param("match") MatchRequest match,
                                       @Param("user") User user,
                                       Pageable pageable);
                                       **/
}

