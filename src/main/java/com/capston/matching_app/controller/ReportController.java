package com.capston.matching_app.controller;

import com.capston.matching_app.dto.ReportRequestDTO;
import com.capston.matching_app.entity.Report;
import com.capston.matching_app.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor //final 필드 자동 주입
public class ReportController {
    private final ReportService reportService;

    /**
     * 새로운 신고 등록
     * @param dto 클라이언트에서 전달받은 신고 정보 DTO
     * @return 성공메시지와 저장된 신고 id
     */
    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody ReportRequestDTO dto){
        //서비스 레이어에서 신고 처리 후 report 엔티티 반환
        Report report = reportService.createReport(dto);
        return ResponseEntity.ok("신고가 접수되었습니다. id: "+report.getId());
    }
}
