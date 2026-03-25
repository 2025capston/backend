package com.capston.matching_app.repository;

import com.capston.matching_app.entity.MatchRequest;
import com.capston.matching_app.entity.OutfitSubmission;
import com.capston.matching_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OutfitSubmissionRepository extends JpaRepository<OutfitSubmission, Long> {

    Optional<OutfitSubmission> findByMatchRequestAndUserAndSubmissionDate(
            MatchRequest matchRequest, User user, LocalDate submissionDate);

    // 같은 날짜에 내 것과 상대방 것 2건 조회 (구현에 맞춰 수정 가능)
    @Query("""
           select s
             from OutfitSubmission s
            where s.matchRequest = :matchRequest
              and s.submissionDate = :date
              and (s.user = :me or s.user = :counterpart)
           """)
    List<OutfitSubmission> findPairOfDay(MatchRequest matchRequest, User me, User counterpart, LocalDate date);

    // 내 히스토리 리스트
    @Query("""
           select s
             from OutfitSubmission s
            where s.matchRequest = :matchRequest
              and s.user = :me
            order by s.submissionDate desc
           """)
    List<OutfitSubmission> findHistory(MatchRequest matchRequest, User me);
}
