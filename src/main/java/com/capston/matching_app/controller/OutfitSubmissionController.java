package com.capston.matching_app.controller;

import com.capston.matching_app.entity.OutfitSubmission;
import com.capston.matching_app.service.OutfitSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/outfit-submissions")
@RequiredArgsConstructor
public class OutfitSubmissionController {

    private final OutfitSubmissionService outfitSubmissionService;

    @PostMapping
    public ResponseEntity<OutfitSubmission> submitOutfit(
            @RequestParam Long matchRequestId,
            @RequestParam Long userId,
            @RequestParam String imageUrl
    ) {
        OutfitSubmission submission = outfitSubmissionService.submitOutfit(matchRequestId, userId, imageUrl);
        return ResponseEntity.ok(submission);
    }
}

