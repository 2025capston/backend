package com.capston.matching_app.service;

import com.capston.matching_app.dto.MatchRequestDTO;
import com.capston.matching_app.entity.*;
import com.capston.matching_app.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
//final필드나 @NonNull이 붙은 필드에 대해서만 자동으로 생성자 만들어줌 -> 생성자주입
public class MatchRequestService {

    //생성자 주입되는 Repository -> DB접근 담당
    private final MatchRequestRepository matchRequestRepository;
    private final MatchDateOptionRepository matchDateOptionRepository;
    private final MatchPlaceOptionRepository matchPlaceOptionRepository;
    private final MatchTimeOptionRepository matchTimeOptionRepository;
    private final MatchSelectionRepository matchSelectionRepository;

    //매칭 신청 생성
    public MatchRequestDTO createMatchRequest(MatchRequestDTO dto, User fromUser,User toUser) {
        //DTO -> 엔터티 변환 + 현재 시간 설정
        MatchRequest matchRequest = MatchRequest.builder()
                .fromUser(fromUser) //신청자
                .toUser(toUser) //신청대기자
                .status(MatchStatus.valueOf(dto.getStatus())) //상태 : 대기, 수락, 거절
                .requestTime(LocalDateTime.now()) //현재시각
                .meetingResult(MeetingResult.valueOf(dto.getMeetingResult()))//매칭결과
                .matchKeepStatus(MatchKeepStatus.valueOf(dto.getMatchKeepStatus())) //유지여부
                .build();
        //DB저장
        MatchRequest saved = matchRequestRepository.save(matchRequest);

        //저장된 엔터티 -> dto 변환 후 반환
        return convertToDTO(saved);
    }

    //모든 매칭 신청 조회
    public List<MatchRequestDTO> getAllRequests(){
        return matchRequestRepository.findAll() //DB에서 모든 매칭 요청 조회
                .stream()
                .map(this::convertToDTO) //엔터티 ->DTO 변환
                .collect(Collectors.toList());
    }

    //엔터티를 DTO로 변환하는 유틸리티 메서드
    private MatchRequestDTO convertToDTO(MatchRequest entity) {
        return MatchRequestDTO.builder()
                .id(entity.getId()) // PK
                .fromUserId(Long.valueOf(entity.getFromUser().getUserId())) // 신청자 ID
                .toUserId(Long.valueOf(entity.getToUser().getUserId()))     // 신청 대상자 ID
                .status(entity.getStatus().name())              // 상태
                .requestTime(entity.getRequestTime())         // 신청 시간
                .meetingResult(String.valueOf(entity.getMeetingResult()))     // 매칭 결과
                .matchKeepStatus(String.valueOf(entity.getMatchKeepStatus())) // 유지 여부
                .build();
    }

    @Transactional
    public void proposeSchedule(Long requestId, List<LocalDate> dates, List<String> places, List<LocalTime> times){
        MatchRequest request = matchRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Match Request not found"));
        if (request.getStatus() != MatchStatus.ACCEPTED){
            throw new IllegalStateException("Schedule can be proposed after acceptance");
        }
        //날짜 저장
        for (LocalDate date : dates){
            MatchDateOption dateOption = matchDateOptionRepository.save(
                    new MatchDateOption(request, date)
            );
            //시간 저장
            for(LocalTime time : times){
                matchTimeOptionRepository.save(new MatchTimeOption(dateOption,time));
            }
        }
        //장소 저장
        for (String place : places){
            matchPlaceOptionRepository.save(
                    new MatchPlaceOption(request, place)
            );
        }
    }

    @Transactional
    public void confirmSchedule(Long requestId, Long dateOptionId, Long timeOptionId, Long placeOptionId){
        MatchRequest request = matchRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Match Request not found"));
        //이미 확정된 경우 방지
        if(request.getStatus() == MatchStatus.CONFIRMED){
            throw new IllegalStateException("Schedule already confirmed");
        }
        //후보 엔터티 조회
        MatchDateOption dateOption = matchDateOptionRepository.findById(dateOptionId)
                .orElseThrow(() -> new RuntimeException("Date option not found"));
        MatchTimeOption timeOption = matchTimeOptionRepository.findById(timeOptionId)
                .orElseThrow(() -> new RuntimeException("Time option not found"));
        MatchPlaceOption placeOption = matchPlaceOptionRepository.findById(placeOptionId)
                .orElseThrow(() -> new RuntimeException("Place option not found"));
        //매칭 신청과 후보가 같은 요청에 속해 있는지 검증
        if (!dateOption.getMatchRequest().getId().equals(request.getId()) ||
                !placeOption.getMatchRequest().getId().equals(request.getId()) ||
                !timeOption.getDateOption().getMatchRequest().getId().equals(request.getId())) {
            throw new IllegalArgumentException("Options do not belong to this request");
        }
        //MatchSelection 저장
        MatchSelection selection = new MatchSelection(request, dateOption, timeOption,placeOption);
        matchSelectionRepository.save(selection);
        //상태를 confirmed로 변경
        request.setStatus(MatchStatus.CONFIRMED);
        matchRequestRepository.save(request);
    }

    @Transactional
    public void cancelMatchRequest(Long requestId, Long userId) {
        MatchRequest matchRequest = matchRequestRepository.findById(requestId)
                .orElseThrow(()-> new RuntimeException("매칭 신청이 존재하지 않습니다."));
        //이미 취소된 경우 방지
        if(matchRequest.getStatus()==MatchStatus.CANCELLED){
            throw new RuntimeException("이미 취소된 매칭입니다.");
        }
        matchRequest.setStatus(MatchStatus.CANCELLED);
        matchRequestRepository.save(matchRequest);
    }
    @Transactional
    public void updateMeetingResult(Long requestId, String result) {
        MatchRequest request = matchRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("매칭 신청이 존재하지 않습니다."));
        request.setMeetingResult(MeetingResult.valueOf(result)); // "SUCCESS" or "FAIL"
        matchRequestRepository.save(request);
    }

    @Transactional
    public void updateMatchKeepStatus(Long requestId, String status) {
        MatchRequest request = matchRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("매칭 신청이 존재하지 않습니다."));
        if (!"SUCCESS".equals(request.getMeetingResult())) {
            throw new IllegalStateException("성사된 만남만 유지/해제할 수 있습니다.");
        }
        request.setMatchKeepStatus(MatchKeepStatus.valueOf(status)); // "KEEP" or "END"
        matchRequestRepository.save(request);
    }


}
