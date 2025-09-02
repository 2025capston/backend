package com.capston.matching_app.repository;

import com.capston.matching_app.entity.MatchRequest;
import com.capston.matching_app.entity.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

    // 기존 메서드 유지
    List<MatchRequest> findByStatus(MatchStatus status);
    // 매치 ID와 참여자 이메일로 권한 검증 겸 조회 (전화번호 전송/조회 API에서 사용)
    @Query("select m from MatchRequest m " +
            "where m.id = :id and (m.fromUser.email = :email or m.toUser.email = :email)")
    Optional<MatchRequest> findByIdAndParticipantEmail(@Param("id") Long id, @Param("email") String email);
    // 두 사용자 조합으로 매치 조회(필요 시) - from/to 순서 상관없이
    @Query("select m from MatchRequest m " +
            "where (m.fromUser.userId = :userA and m.toUser.userId = :userB) " +
            "   or (m.fromUser.userId = :userB and m.toUser.userId = :userA)")
    Optional<MatchRequest> findBetweenUsers(@Param("userA") Integer userA, @Param("userB") Integer userB);
}