package com.capston.matching_app.service;

import com.capston.matching_app.entity.IdealTypeProfile;
import com.capston.matching_app.repository.IdealTypeProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdealTypeService {

    private final IdealTypeProfileRepository idealTypeProfileRepository;

    /** 이상형 선호: 성별만 저장 (나이 범위 제거, 임베딩 저장 없음) */
    @Transactional
    public void savePreferencesOnly(Integer userId, String matchingGender) {
        IdealTypeProfile p = idealTypeProfileRepository.findById(userId)
                .orElseGet(() -> {
                    IdealTypeProfile np = new IdealTypeProfile();
                    np.setUserId(userId);
                    return np; // olderThan/youngerThan 등은 엔티티 기본값/DB 기본값 유지
                });

        p.setMatchingGender(normalizeGender(matchingGender));
        // ⚠️ 나이 관련 필드는 더 이상 사용하지 않음 → 변경하지 않고 그대로 둡니다.
        idealTypeProfileRepository.save(p);
    }

    /** "여성/남성/둘다" 등 → "FEMALE/MALE/BOTH" 정규화 */
    public String normalizeGender(String input) {
        if (input == null) return "BOTH";
        String s = input.trim().toLowerCase();
        if (s.equals("남성") || s.equals("male") || s.equals("m")) return "MALE";
        if (s.equals("여성") || s.equals("female") || s.equals("f")) return "FEMALE";
        if (s.equals("둘다") || s.equals("both") || s.equals("all")) return "BOTH";
        return "BOTH";
    }
}
