package com.capston.matching_app.controller;

import com.capston.matching_app.dto.auth.UserResponseDTO;
import com.capston.matching_app.entity.User;
import com.capston.matching_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;


    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
        return ResponseEntity.ok(new UserResponseDTO(user.getUserId(), user.getName(), user.getEmail(), user.getPhoneNumber()));
    }
}
