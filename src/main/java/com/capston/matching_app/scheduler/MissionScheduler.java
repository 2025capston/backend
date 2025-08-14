package com.capston.matching_app.scheduler;

import com.capston.matching_app.service.MatchMissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component //스프링 빈으로 등록(스케줄러 동작 가능)
@RequiredArgsConstructor //final 필드 자동 주입
public class MissionScheduler {

    //미션 배정 로직을 담고 있는 서비스
    private final MatchMissionService matchMissionService;

    //매일 오후4시 8분 6초
    //cron = "6 8 16 * * ?" 이건 초/분/시/일/월/요일 임 * * ? 매일, 요일 무관
    //zone은 한국 시간대 기준
    @Scheduled(cron = "6 8 16 * * ?", zone ="Asia/Seoul")
    public void assignDailyMissionsAtTime(){
        matchMissionService.assignDailyMission();
    }
}
