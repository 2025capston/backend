package com.capston.matching_app.repository;

import com.capston.matching_app.entity.MatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {
    //커스텀 쿼리가 필요하면 여기 작성
}
