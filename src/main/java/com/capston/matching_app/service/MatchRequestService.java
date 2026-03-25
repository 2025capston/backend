package com.capston.matching_app.service;

import com.capston.matching_app.dto.MatchRequestDTO;
import com.capston.matching_app.dto.PhoneShareResponseDTO;
import com.capston.matching_app.entity.*;
import com.capston.matching_app.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchRequestService {

    private final MatchRequestRepository matchRequestRepository;
    private final MatchDateOptionRepository matchDateOptionRepository;
    private final MatchPlaceOptionRepository matchPlaceOptionRepository;
    private final MatchTimeOptionRepository matchTimeOptionRepository;
    private final MatchSelectionRepository matchSelectionRepository;

    // ---------- 공통 유틸 ----------
    private MatchRequest loadAndVerify(Long matchId, String email) {
        return matchRequestRepository.findByIdAndParticipantEmail(matchId, email)
                .orElseThrow(() -> new AccessDeniedException("매칭 당사자만 접근할 수 있습니다. matchId=" + matchId));
    }

    private boolean callerIsFrom(MatchRequest m, String email) {
        return m.getFromUser().getEmail().equals(email);
    }

    private PhoneShareResponseDTO toPhoneDto(MatchRequest m, String callerEmail) {
        boolean bothKept   = (m.getMatchKeepStatus() == MatchKeepStatus.KEEP);
        boolean bothShared = m.isSharePhoneFrom() && m.isSharePhoneTo();

        User me = callerIsFrom(m, callerEmail) ? m.getFromUser() : m.getToUser();
        User partner = (me == m.getFromUser()) ? m.getToUser() : m.getFromUser();

        return PhoneShareResponseDTO.builder()
                .bothKept(bothKept)
                .bothShared(bothShared)
                .myPhoneNumber(me.getPhoneNumber())
                .partnerPhoneNumber(bothShared ? partner.getPhoneNumber() : null)
                .phoneExchangedAt(m.getPhoneExchangedAt())
                .build();
    }

    private MatchRequestDTO toMatchDto(MatchRequest entity) {
        return MatchRequestDTO.builder()
                .id(entity.getId())
                .fromUserId(Long.valueOf(entity.getFromUser().getUserId()))
                .toUserId(Long.valueOf(entity.getToUser().getUserId()))
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .requestTime(entity.getRequestTime())
                .meetingResult(entity.getMeetingResult() != null ? entity.getMeetingResult().name() : null)
                .matchKeepStatus(entity.getMatchKeepStatus() != null ? entity.getMatchKeepStatus().name() : null)
                // 전화번호 교환 상태 추가
                .sharePhoneFrom(entity.isSharePhoneFrom())
                .sharePhoneTo(entity.isSharePhoneTo())
                .phoneExchangedAt(entity.getPhoneExchangedAt())
                .build();
    }

    // ---------- 기존 기능 ----------
    public MatchRequestDTO createMatchRequest(MatchRequestDTO dto, User fromUser, User toUser) {
        MatchRequest matchRequest = MatchRequest.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .status(dto.getStatus() != null ? MatchStatus.valueOf(dto.getStatus()) : MatchStatus.PENDING)
                .requestTime(LocalDateTime.now())
                .meetingResult(dto.getMeetingResult() != null ? MeetingResult.valueOf(dto.getMeetingResult()) : null)
                .matchKeepStatus(dto.getMatchKeepStatus() != null ? MatchKeepStatus.valueOf(dto.getMatchKeepStatus()) : null)
                .build();
        MatchRequest saved = matchRequestRepository.save(matchRequest);
        return toMatchDto(saved);
    }

    public List<MatchRequestDTO> getAllRequests() {
        return matchRequestRepository.findAll()
                .stream()
                .map(this::toMatchDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void proposeSchedule(Long requestId, List<LocalDate> dates, List<String> places, List<LocalTime> times) {
        MatchRequest request = matchRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Match Request not found"));
        if (request.getStatus() != MatchStatus.ACCEPTED) {
            throw new IllegalStateException("Schedule can be proposed after acceptance");
        }
        for (LocalDate date : dates) {
            MatchDateOption dateOption = matchDateOptionRepository.save(new MatchDateOption(request, date));
            for (LocalTime time : times) {
                matchTimeOptionRepository.save(new MatchTimeOption(dateOption, time));
            }
        }
        for (String place : places) {
            matchPlaceOptionRepository.save(new MatchPlaceOption(request, place));
        }
    }

    @Transactional
    public void confirmSchedule(Long requestId, Long dateOptionId, Long timeOptionId, Long placeOptionId) {
        MatchRequest request = matchRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Match Request not found"));
        if (request.getStatus() == MatchStatus.CONFIRMED) {
            throw new IllegalStateException("Schedule already confirmed");
        }
        MatchDateOption dateOption = matchDateOptionRepository.findById(dateOptionId)
                .orElseThrow(() -> new RuntimeException("Date option not found"));
        MatchTimeOption timeOption = matchTimeOptionRepository.findById(timeOptionId)
                .orElseThrow(() -> new RuntimeException("Time option not found"));
        MatchPlaceOption placeOption = matchPlaceOptionRepository.findById(placeOptionId)
                .orElseThrow(() -> new RuntimeException("Place option not found"));

        if (!dateOption.getMatchRequest().getId().equals(request.getId())
                || !placeOption.getMatchRequest().getId().equals(request.getId())
                || !timeOption.getDateOption().getMatchRequest().getId().equals(request.getId())) {
            throw new IllegalArgumentException("Options do not belong to this request");
        }
        //Dirty Checking 활용 : @Transactional 안에서 엔티티 상태 변경하면 메서드 종료될 때 자동으로 DB 반영됨
        //불필요한 save() 제거
        //matchSelectionRepository.save(new MatchSelection(request, dateOption, timeOption, placeOption));
        request.setStatus(MatchStatus.CONFIRMED);
        //matchRequestRepository.save(request);
    }

    @Transactional
    public void cancelMatchRequest(Long requestId, Long userId) {
        MatchRequest matchRequest = matchRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("매칭 신청이 존재하지 않습니다."));
        if (matchRequest.getStatus() == MatchStatus.CANCELLED) {
            throw new RuntimeException("이미 취소된 매칭입니다.");
        }
        matchRequest.setStatus(MatchStatus.CANCELLED);
        //matchRequestRepository.save(matchRequest);
    }

    @Transactional
    public void updateMeetingResult(Long requestId, String result) {
        MatchRequest request = matchRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("매칭 신청이 존재하지 않습니다."));
        request.setMeetingResult(result != null ? MeetingResult.valueOf(result) : null);
        //matchRequestRepository.save(request);
    }

    @Transactional
    public void updateMatchKeepStatus(Long requestId, String status) {
        MatchRequest request = matchRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("매칭 신청이 존재하지 않습니다."));
        // 🔧 버그 수정: 문자열 비교가 아니라 Enum 비교
        if (request.getMeetingResult() != MeetingResult.SUCCESS) {
            throw new IllegalStateException("성사된 만남만 유지/해제할 수 있습니다.");
        }
        request.setMatchKeepStatus(status != null ? MatchKeepStatus.valueOf(status) : null);
        //matchRequestRepository.save(request);
    }

    // ---------- 신규: 전화번호 교환 ----------
    @Transactional
    public PhoneShareResponseDTO toggleSharePhone(Long matchId, String callerEmail, boolean share) {
        MatchRequest m = loadAndVerify(matchId, callerEmail);

        // 규칙: 최종 매칭 유지가 KEEP일 때만 공유 허용 (원하면 제거 가능)
        if (share && m.getMatchKeepStatus() != MatchKeepStatus.KEEP) {
            throw new IllegalStateException("둘 다 '매칭 유지(KEEP)' 선택이 완료되어야 번호를 전송할 수 있습니다.");
        }

        if (callerIsFrom(m, callerEmail)) {
            m.setSharePhoneFrom(share);
        } else {
            m.setSharePhoneTo(share);
        }

        if (m.isSharePhoneFrom() && m.isSharePhoneTo() && m.getPhoneExchangedAt() == null) {
            m.setPhoneExchangedAt(LocalDateTime.now());
        }
        return toPhoneDto(m, callerEmail);
    }

    @Transactional
    public PhoneShareResponseDTO getPhoneShareStatus(Long matchId, String callerEmail) {
        MatchRequest m = loadAndVerify(matchId, callerEmail);
        return toPhoneDto(m, callerEmail);
    }
}
