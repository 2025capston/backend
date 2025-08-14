package com.capston.matching_app.repository;

import com.capston.matching_app.entity.MatchRequest;
import com.capston.matching_app.entity.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {
    //커스텀 쿼리가 필요하면 여기 작성
    List<MatchRequest> findByStatus(MatchStatus status);
}
