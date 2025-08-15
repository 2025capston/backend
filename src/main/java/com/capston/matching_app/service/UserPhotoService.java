package com.capston.matching_app.service;

import com.capston.matching_app.entity.UserPhoto;
import com.capston.matching_app.repository.UserPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPhotoService {

    private final FileStorageService fileStorageService;
    private final UserPhotoRepository userPhotoRepository;

    /** 업로드: isProfile=true면 기존 대표 해제 후 저장 (DB UNIQUE로 2중 보증) */
    @Transactional
    public UserPhoto upload(Integer userId, MultipartFile file, boolean isProfile) {
        String url = fileStorageService.saveUserPhoto(userId, file);

        if (isProfile) {
            userPhotoRepository.clearProfileByUserId(userId);
        }

        UserPhoto photo = new UserPhoto();
        photo.setUserId(userId);
        photo.setPhotoUrl(url);
        photo.setIsProfile(isProfile);

        return userPhotoRepository.save(photo);
    }

    /** 유저 사진 목록 */
    @Transactional(readOnly = true)
    public List<UserPhoto> list(Integer userId) {
        return userPhotoRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /** 기존 사진들 중 대표만 변경 (원샷) */
    @Transactional
    public void setProfile(Integer userId, Integer photoId) {
        UserPhoto target = userPhotoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("photo not found: " + photoId));
        if (!target.getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한 없음(본인 사진이 아님)");
        }
        userPhotoRepository.clearProfileByUserId(userId); // 모두 false
        userPhotoRepository.setProfile(userId, photoId);  // 대상 true
    }

    /** 사진 삭제 */
    @Transactional
    public boolean delete(Integer userId, Integer photoId) {
        long deleted = userPhotoRepository.deleteByUserIdAndId(userId, photoId);
        return deleted > 0;
    }
}
