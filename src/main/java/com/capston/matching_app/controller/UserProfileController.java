package com.capston.matching_app.controller;

import com.capston.matching_app.dto.UserProfileRequestDTO;
import com.capston.matching_app.dto.UserProfileResponseDTO;
import com.capston.matching_app.security.CustomUserDetails;
import com.capston.matching_app.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/profile")  // PathVariable 없이 로그인 사용자 기준
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<UserProfileResponseDTO> getProfile() {
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        Integer userId = user.getUserId();

        var dto = userProfileService.getProfile(userId);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping
    public ResponseEntity<UserProfileResponseDTO> saveOrUpdate(@RequestBody UserProfileRequestDTO req) {
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        Integer userId = user.getUserId();

        // 보안상 userId는 서버에서 강제; 클라이언트 body의 userId는 무시해도 됨
        req.setUserId(userId);

        var dto = userProfileService.saveOrUpdateProfile(userId, req);
        return ResponseEntity.ok(dto);
    }
}
