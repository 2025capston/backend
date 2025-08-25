package com.capston.matching_app.controller;

import com.capston.matching_app.dto.UserProfileDTO;
import com.capston.matching_app.security.CustomUserDetails;
import com.capston.matching_app.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/profile")  // PathVariable 제거
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<UserProfileDTO> getProfile() {
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        Integer userId = user.getUserId();
        UserProfileDTO dto = userProfileService.getProfile(userId);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping
    public ResponseEntity<UserProfileDTO> saveOrUpdate(@RequestBody UserProfileDTO dto) {
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        Integer userId = user.getUserId();
        return ResponseEntity.ok(userProfileService.saveOrUpdateProfile(userId, dto));
    }
}
