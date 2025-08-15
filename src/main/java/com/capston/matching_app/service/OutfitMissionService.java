package com.capston.matching_app.service;

import com.capston.matching_app.entity.MatchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutfitMissionService {
    //미션 생성 및 알림
    //OutfitSubmissionService는 제출 처리
    public void createOutfitMission(MatchRequest matchRequest) {
        // 실제로는 푸시 알림 / 메시지 발송 등
        System.out.println("착장 미션 생성 - 매칭 ID: " + matchRequest.getId());
    }
}
