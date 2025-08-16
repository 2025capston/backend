package com.capston.matching_app.service;

import com.capston.matching_app.dto.MissionResponseDTO;
import com.capston.matching_app.entity.MatchMission;
import com.capston.matching_app.entity.MissionResponse;
import com.capston.matching_app.entity.User;
import com.capston.matching_app.repository.MatchMissionRepository;
import com.capston.matching_app.repository.MissionResponseRepository;
import com.capston.matching_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
        User user = userRepository.findById(Math.toIntExact(userId))
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 사용자입니다."));
        MissionResponse response = MissionResponse.builder()
                .matchMission(matchMission)
                .user(user)
                .answer(answer)
                .submittedAt(LocalDateTime.now())
                .build();

        return missionResponseRepository.save(response);
    }

    public MissionResponseDTO getMissionResponses (Long matchMissionId, Long userId){
        //1. 해당 미션에 제출된 응답 모두 조회
        List<MissionResponse> responses = missionResponseRepository.findByMatchMissionId(matchMissionId);

        if(responses.size() < 2){
            throw new IllegalStateException("아직 상대방이 응답을 제출하지 않았습니다.");
        }

        //2. 요청한 사용자와 상대방 응답 구분
        MissionResponse myResponse = responses.stream()
                .filter(r -> r.getUser().getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("내 응답을 찾을 수 없습니다."));

        MissionResponse partnerResponse = responses.stream()
                .filter(r -> !r.getUser().getUserId().equals(userId))
                .findFirst()
                .orElseThrow(()->new IllegalArgumentException("상대방 응답을 찾을 수 없습니다."));
        //3.DTO로 변환
        return MissionResponseDTO.builder()
                .myAnswer(myResponse.getAnswer())
                .partnerAnswer(partnerResponse.getAnswer())
                .build();
    }

}
