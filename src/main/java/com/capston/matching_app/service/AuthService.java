package com.capston.matching_app.service;

import com.capston.matching_app.dto.auth.*;
import com.capston.matching_app.entity.User;
import com.capston.matching_app.repository.UserRepository;
import com.capston.matching_app.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;


    @Transactional
    public UserResponseDTO signup(SignupRequestDTO req) {
        String email = req.getEmail().trim().toLowerCase(); // 이메일 정규화 권장
        String name  = req.getName().trim();
        String phone = req.getPhoneNumber().trim();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (!req.getPassword().equals(req.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(req.getPassword())) // 해시만 저장
                .phoneNumber(phone)
                .build();
        User saved = userRepository.save(user);
        return new UserResponseDTO(saved.getUserId(), saved.getName(), saved.getEmail(), saved.getPhoneNumber());
    }



    public TokenResponseDTO login(LoginRequestDTO req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));


        String access = jwtTokenProvider.createAccessToken(req.getEmail(), Map.of("role", "ROLE_USER"));
        String refresh = jwtTokenProvider.createRefreshToken(req.getEmail());
        return new TokenResponseDTO(access, refresh,"Bearer");
    }


    public TokenResponseDTO refresh(RefreshRequestDTO req) {
        String email = jwtTokenProvider.parse(req.getRefreshToken()).getBody().getSubject();
        String access = jwtTokenProvider.createAccessToken(email, Map.of("role", "ROLE_USER"));
        String refresh = jwtTokenProvider.createRefreshToken(email);
        return new TokenResponseDTO(access, refresh,"Bearer");
    }
}
