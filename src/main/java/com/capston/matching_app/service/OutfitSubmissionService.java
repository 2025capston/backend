package com.capston.matching_app.service;

import com.capston.matching_app.entity.MatchRequest;
import com.capston.matching_app.entity.OutfitSubmission;
import com.capston.matching_app.entity.User;
import com.capston.matching_app.repository.MatchRequestRepository;
import com.capston.matching_app.repository.OutfitSubmissionRepository;
import com.capston.matching_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutfitSubmissionService {

    private final OutfitSubmissionRepository outfitSubmissionRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.base-dir}")
    private String baseDir; // ex) /var/app/uploads

    @Value("${app.upload.public-prefix:/static/user-photos}")
    private String publicPrefix; // ex) /static/user-photos

    private static final String SUBDIR = "outfits"; // 하위 폴더

    public OutfitSubmission submitOutfit(Long matchRequestId, Integer userId, MultipartFile file) {
        MatchRequest matchRequest = matchRequestRepository.findById(matchRequestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매칭입니다."));

        User user = userRepository.findById(Math.toIntExact(userId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비었습니다.");
        }

        // 파일명 안전 처리
        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("image");
        String safeName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = UUID.randomUUID() + "_" + safeName;

        // 실제 저장 경로: {baseDir}/outfits/{fileName}
        Path targetDir = Paths.get(baseDir, SUBDIR).toAbsolutePath().normalize();
        Path target = targetDir.resolve(fileName);
        try {
            Files.createDirectories(targetDir);
            // transferTo(Path)가 없을 수 있으므로 File로
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }

        // 공개 URL: {publicPrefix}/outfits/{fileName}
        String normalizedPrefix = publicPrefix.endsWith("/") ? publicPrefix.substring(0, publicPrefix.length() - 1) : publicPrefix;
        String imageUrl = normalizedPrefix + "/" + SUBDIR + "/" + fileName;

        // (선택) 하루 1회 제한이 필요하면 여기서 existsBy... 체크
        // if (outfitSubmissionRepository.existsByMatchRequestAndUserAndSubmissionDate(matchRequest, user, LocalDate.now())) {
        //     throw new IllegalStateException("오늘은 이미 제출했습니다.");
        // }

        OutfitSubmission submission = OutfitSubmission.builder()
                .matchRequest(matchRequest)
                .user(user)
                .imageUrl(imageUrl)
                .submissionDate(LocalDate.now())
                .submittedAt(LocalDateTime.now())
                .build();

        return outfitSubmissionRepository.save(submission);
    }


}

