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

    /** 이상형 선호만 저장 (임베딩 저장 없음) */
    @Transactional
    public void savePreferencesOnly(Integer userId, String matchingGender, Integer olderThan, Integer youngerThan) {
        IdealTypeProfile p = idealTypeProfileRepository.findById(userId).orElseGet(() -> {
            IdealTypeProfile np = new IdealTypeProfile();
            np.setUserId(userId);
            return np;
        });
        p.setMatchingGender(normalizeGender(matchingGender));
        p.setOlderThan(olderThan != null ? Math.max(0, olderThan) : 0);
        p.setYoungerThan(youngerThan != null ? Math.max(0, youngerThan) : 0);
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
