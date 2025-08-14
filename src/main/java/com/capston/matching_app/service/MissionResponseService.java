package com.capston.matching_app.service;

import com.capston.matching_app.entity.MatchMission;
import com.capston.matching_app.entity.MissionResponse;
import com.capston.matching_app.entity.User;
import com.capston.matching_app.repository.MatchMissionRepository;
import com.capston.matching_app.repository.MissionResponseRepository;
import com.capston.matching_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MissionResponseService {

    private final MissionResponseRepository missionResponseRepository;
    private final MatchMissionRepository matchMissionRepository;
    private final UserRepository userRepository;

    public MissionResponse submitResponse(Long matchMissionId, Long userId, String answer){
        MatchMission matchMission = matchMissionRepository.findById(matchMissionId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 매칭 미션입니다."));

        //이거 지금 userRepository없어서 -> 나중에 팀원한테 받고 수정할 예정
        User user = userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 사용자입니다."));
        MissionResponse response = MissionResponse.builder()
                .matchMission(matchMission)
                .user(user)
                .answer(answer)
                .submittedAt(LocalDateTime.now())
                .build();

        return missionResponseRepository.save(response);
    }
}
