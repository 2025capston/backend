package com.capston.matching_app.service;

import com.capston.matching_app.dto.UserProfileRequestDTO;
import com.capston.matching_app.dto.UserProfileResponseDTO;
import com.capston.matching_app.entity.Region;
import com.capston.matching_app.entity.Subregion;
import com.capston.matching_app.entity.User;
import com.capston.matching_app.entity.UserProfile;
import com.capston.matching_app.repository.RegionRepository;
import com.capston.matching_app.repository.SubregionRepository;
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
    private final RegionRepository regionRepository;
    private final SubregionRepository subregionRepository;

    @Transactional(readOnly = true)
    public UserProfileResponseDTO getProfile(Integer userId) {
        return userProfileRepository.findById(userId)
                .map(UserProfileResponseDTO::from)
                .orElse(null);
    }

    /**
     * 프로필 업서트
     * - 지역 값 둘 다 null: 지역 미설정(비움)
     * - 지역 값 둘 다 존재: 존재/소속 검증 후 세팅
     * - 하나만 존재: 400(BAD_REQUEST)
     */
    @Transactional
    public UserProfileResponseDTO saveOrUpdateProfile(Integer userId, UserProfileRequestDTO req) {
        // 1) 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // 2) upsert 로드 (없으면 새로 생성)
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    UserProfile up = new UserProfile();
                    up.setUserId(user.getUserId()); // PK = users.user_id
                    return up;
                });

        // 3) 기본 프로필 필드 복사
        profile.setGender(req.getGender());
        profile.setBirthYear(req.getBirthYear());
        profile.setHeight(req.getHeight());
        profile.setSexualOrientation(req.getSexualOrientation());

        // 4) 지역 처리 (NULL 허용)
        Long regionId = req.getRegionId();
        Long subregionId = req.getSubregionId();

        if (regionId == null && subregionId == null) {
            // 둘 다 비우기(미설정 상태 허용)
            profile.setRegion(null);
            profile.setSubregion(null);
        } else if (regionId != null && subregionId != null) {
            // 둘 다 있을 때만 검증 및 세팅
            Region region = regionRepository.findById(regionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid regionId"));
            Subregion subregion = subregionRepository.findById(subregionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid subregionId"));

            if (!subregion.getRegion().getId().equals(region.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "subregion does not belong to region");
            }
            profile.setRegion(region);
            profile.setSubregion(subregion);
        } else {
            // 하나만 온 경우는 허용하지 않음(데이터 일관성 위해)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both regionId and subregionId must be provided together, or both null");
        }

        // 5) 저장 후 응답
        UserProfile saved = userProfileRepository.save(profile);
        return UserProfileResponseDTO.from(saved);
    }
}
