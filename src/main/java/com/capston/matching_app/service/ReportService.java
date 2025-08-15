package com.capston.matching_app.service;

import com.capston.matching_app.dto.ReportRequestDTO;
import com.capston.matching_app.entity.MatchRequest;
import com.capston.matching_app.entity.Report;
import com.capston.matching_app.entity.User;
import com.capston.matching_app.repository.MatchRequestRepository;
import com.capston.matching_app.repository.ReportRepository;
import com.capston.matching_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final UserRepository userRepository;

    public Report createReport(ReportRequestDTO dto){
        MatchRequest matchRequest = matchRequestRepository.findById(dto.getRequestId())
                .orElseThrow(()-> new RuntimeException("존재하지 않는 매칭 요청입니다."));
        User reporter = userRepository.findById(dto.getReporterUserId())
                .orElseThrow(()-> new RuntimeException("존재하지 않는 사용자입니다."));
        Report report = Report.builder()
                .matchRequest(matchRequest)
                .reporter(reporter)
                .reason(dto.getReason())
                .details(dto.getDetails())
                .reportTime(LocalDateTime.now())
                .build();
        return reportRepository.save(report);
    }
}
