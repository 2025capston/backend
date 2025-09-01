package com.capston.matching_app.service;

import com.capston.matching_app.dto.OutfitViewDTO;

import com.capston.matching_app.entity.MatchRequest;
import com.capston.matching_app.entity.OutfitSubmission;
import com.capston.matching_app.entity.OutfitVisibilityStatus;
import com.capston.matching_app.entity.User;
import com.capston.matching_app.repository.MatchRequestRepository;
import com.capston.matching_app.repository.OutfitSubmissionRepository;
import com.capston.matching_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OutfitSubmissionService {

    private final OutfitSubmissionRepository outfitSubmissionRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final UserRepository userRepository;

    @Value("${app.timezone:Asia/Seoul}")
    private String appTimezone;

    @Value("${app.upload.base-dir}")
    private String baseDir;

    @Value("${app.upload.public-prefix:/static/user-photos}")
    private String publicPrefix;

    private static final String SUBDIR = "outfits";

    @Transactional
    public OutfitViewDTO submitAndView(Long matchRequestId, Long myUserId, MultipartFile file) {
        MatchRequest match = matchRequestRepository.findById(matchRequestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매칭입니다."));

        User me = userRepository.findById(Math.toIntExact(myUserId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        User counterpart = getCounterpartOrThrow(match, me);

        ZoneId zone = ZoneId.of(appTimezone);
        LocalDate today = LocalDate.now(zone);

        if (file == null || file.isEmpty()) throw new IllegalArgumentException("파일이 비었습니다.");

        String ext = getExtension(file.getOriginalFilename());
        String fileName = me.getUserId() + "_" + UUID.randomUUID().toString().replace("-", "") + ext;

        Path dir = Paths.get(baseDir, SUBDIR,
                        String.valueOf(match.getId()),
                        String.format("%04d", today.getYear()),
                        String.format("%02d", today.getMonthValue()),
                        String.format("%02d", today.getDayOfMonth()))
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(fileName).toFile());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }

        // 🔧 matchId 타입 느슨화 (Object)
        String imageUrl = buildPublicUrl(publicPrefix, SUBDIR, match.getId(), today, fileName);

        OutfitSubmission submission = outfitSubmissionRepository
                .findByMatchRequestAndUserAndSubmissionDate(match, me, today)
                .map(s -> { s.setImageUrl(imageUrl); s.setSubmittedAt(LocalDateTime.now(zone)); return s; })
                .orElseGet(() -> OutfitSubmission.builder()
                        .matchRequest(match)
                        .user(me)
                        .imageUrl(imageUrl)
                        .submissionDate(today)
                        .submittedAt(LocalDateTime.now(zone))
                        .build());

        outfitSubmissionRepository.save(submission);

        List<OutfitSubmission> pair = outfitSubmissionRepository.findPairOfDay(match, me, counterpart, today);

        OutfitVisibilityStatus status = OutfitVisibilityStatus.AWAITING_PARTNER;
        String partnerUrl = null;
        if (pair.size() >= 2) {
            partnerUrl = pair.stream()
                    .filter(x -> Objects.equals(x.getUser().getUserId(), counterpart.getUserId()))
                    .map(OutfitSubmission::getImageUrl)
                    .findFirst().orElse(null);
            status = OutfitVisibilityStatus.READY;
        }

        return OutfitViewDTO.builder()
                .matchRequestId(Math.toIntExact(match.getId()))        // DTO 필드 Long 권장
                .date(today)
                .status(status)
                .myPhotoUrl(imageUrl)
                .partnerPhotoUrl(status == OutfitVisibilityStatus.READY ? partnerUrl : null)
                .build();
    }

    @Transactional(readOnly = true)
    public OutfitViewDTO viewOfDate(Long matchRequestId, Long myUserId, LocalDate date) {
        MatchRequest match = matchRequestRepository.findById(matchRequestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매칭입니다."));

        User me = userRepository.findById(Math.toIntExact(myUserId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        User counterpart = getCounterpartOrThrow(match, me);

        Optional<OutfitSubmission> mineOpt =
                outfitSubmissionRepository.findByMatchRequestAndUserAndSubmissionDate(match, me, date);

        List<OutfitSubmission> pair = outfitSubmissionRepository.findPairOfDay(match, me, counterpart, date);
        boolean ready = pair.size() >= 2;

        String myUrl = mineOpt.map(OutfitSubmission::getImageUrl).orElse(null);
        String partnerUrl = ready
                ? pair.stream().filter(x -> Objects.equals(x.getUser().getUserId(), counterpart.getUserId()))
                .map(OutfitSubmission::getImageUrl).findFirst().orElse(null)
                : null;

        return OutfitViewDTO.builder()
                .matchRequestId(Math.toIntExact(match.getId()))
                .date(date)
                .status(ready ? OutfitVisibilityStatus.READY : OutfitVisibilityStatus.AWAITING_PARTNER)
                .myPhotoUrl(myUrl)
                .partnerPhotoUrl(ready ? partnerUrl : null)
                .build();
    }

    @Transactional(readOnly = true)
    public List<OutfitViewDTO> history(Long matchRequestId, Long myUserId, int days) {
        MatchRequest match = matchRequestRepository.findById(matchRequestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매칭입니다."));
        User me = userRepository.findById(Math.toIntExact(myUserId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        User counterpart = getCounterpartOrThrow(match, me);

        ZoneId zone = ZoneId.of(appTimezone);
        LocalDate start = LocalDate.now(zone).minusDays(days);

        // 레포에 페이징 없는 findHistory가 있어야 함
        List<OutfitSubmission> mine = outfitSubmissionRepository.findHistory(match, me);

        return mine.stream()
                .filter(s -> !s.getSubmissionDate().isBefore(start))
                .map(s -> {
                    List<OutfitSubmission> pair = outfitSubmissionRepository.findPairOfDay(match, me, counterpart, s.getSubmissionDate());
                    boolean ready = pair.size() >= 2;
                    String partnerUrl = ready
                            ? pair.stream().filter(x -> Objects.equals(x.getUser().getUserId(), counterpart.getUserId()))
                            .map(OutfitSubmission::getImageUrl).findFirst().orElse(null)
                            : null;
                    return OutfitViewDTO.builder()
                            .matchRequestId(Math.toIntExact(match.getId()))
                            .date(s.getSubmissionDate())
                            .status(ready ? OutfitVisibilityStatus.READY : OutfitVisibilityStatus.AWAITING_PARTNER)
                            .myPhotoUrl(s.getImageUrl())
                            .partnerPhotoUrl(ready ? partnerUrl : null)
                            .build();
                }).toList();
    }

    // --- helpers ---
    private String getExtension(String original) {
        if (original == null) return ".jpg";
        int dot = original.lastIndexOf('.');
        return (dot >= 0 && dot < original.length()-1) ? original.substring(dot) : ".jpg";
    }

    // matchId를 Object로 받아 타입 혼용 제거
    private String buildPublicUrl(String prefix, String subdir, Object matchId, LocalDate d, String fileName) {
        String p = prefix.endsWith("/") ? prefix.substring(0, prefix.length()-1) : prefix;
        return String.format("%s/%s/%s/%04d/%02d/%02d/%s",
                p, subdir, String.valueOf(matchId), d.getYear(), d.getMonthValue(), d.getDayOfMonth(), fileName);
    }

    private User getCounterpartOrThrow(MatchRequest match, User me) {
        User from = match.getFromUser();
        User to   = match.getToUser();

        boolean isFrom = Objects.equals(from.getUserId(), me.getUserId());
        boolean isTo   = Objects.equals(to.getUserId(), me.getUserId());
        if (!isFrom && !isTo) throw new AccessDeniedException("이 매칭의 참여자가 아닙니다.");

        return isFrom ? to : from;
    }
}
