package com.capston.matching_app.service;

import com.capston.matching_app.entity.MatchRequest;
import com.capston.matching_app.entity.OutfitSubmission;
import com.capston.matching_app.entity.User;
import com.capston.matching_app.repository.MatchRequestRepository;
import com.capston.matching_app.repository.OutfitSubmissionRepository;
import com.capston.matching_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutfitSubmissionService {

    private final OutfitSubmissionRepository outfitSubmissionRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final UserRepository userRepository;

    public OutfitSubmission submitOutfit(Long matchRequestId, Long userId, String imageUrl) {
        MatchRequest matchRequest = matchRequestRepository.findById(matchRequestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매칭입니다."));

        User user = userRepository.findById(Math.toIntExact(userId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

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
