package com.capston.matching_app.controller;

import com.capston.matching_app.dto.UserProfileDTO;
import com.capston.matching_app.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Integer userId) {
        UserProfileDTO dto = userProfileService.getProfile(userId);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping
    public ResponseEntity<UserProfileDTO> saveOrUpdate(
            @PathVariable Integer userId,
            @RequestBody UserProfileDTO dto) {
        return ResponseEntity.ok(userProfileService.saveOrUpdateProfile(userId, dto));
    }
}