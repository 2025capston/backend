package com.capston.matching_app.service;

import com.capston.matching_app.dto.UserProfileDTO;
import com.capston.matching_app.entity.User;
import com.capston.matching_app.entity.UserProfile;
import com.capston.matching_app.repository.UserProfileRepository;
import com.capston.matching_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(Integer userId) {
        return userProfileRepository.findById(userId)
                .map(this::toDTO)
                .orElse(null);
    }

    @Transactional
    public UserProfileDTO saveOrUpdateProfile(Integer userId, UserProfileDTO dto) {
        // 유저 존재 확인 (없으면 404)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // 프로필 조회
        UserProfile profile = userProfileRepository.findById(userId).orElse(null);

        if (profile == null) {
            // 신규 생성: ID를 직접 세팅하지 말고, User만 연결
            profile = new UserProfile();
            profile.setUser(user); // @MapsId 가 여기서 user.userId 를 PK로 복사
        } else {
            // 기존 엔티티에도 혹시 user 연관이 비어있지 않도록 보장
            if (profile.getUser() == null) profile.setUser(user);
        }

        // 필드 복사
        profile.setGender(dto.getGender());
        profile.setBirthYear(dto.getBirthYear());
        profile.setHeight(dto.getHeight());
        profile.setCity(dto.getCity());
        profile.setDistrict(dto.getDistrict());
        profile.setSexualOrientation(dto.getSexualOrientation());

        // INSERT or UPDATE
        UserProfile saved = userProfileRepository.save(profile);
        return toDTO(saved);
    }

    private UserProfileDTO toDTO(UserProfile profile) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setGender(profile.getGender());
        dto.setBirthYear(profile.getBirthYear());
        dto.setHeight(profile.getHeight());
        dto.setCity(profile.getCity());
        dto.setDistrict(profile.getDistrict());
        dto.setSexualOrientation(profile.getSexualOrientation());
        return dto;
    }
}
