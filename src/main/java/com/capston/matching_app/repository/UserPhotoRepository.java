package com.capston.matching_app.repository;

import com.capston.matching_app.entity.UserPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserPhotoRepository extends JpaRepository<UserPhoto, Integer> {

    List<UserPhoto> findByUserIdOrderByCreatedAtDesc(Integer userId);

    Optional<UserPhoto> findFirstByUserIdAndIsProfileTrue(Integer userId);

    long deleteByUserIdAndId(Integer userId, Integer id);

    /** 기존 대표 모두 false */
    @Modifying
    @Query("UPDATE UserPhoto p SET p.isProfile = false WHERE p.userId = :userId AND p.isProfile = true")
    int clearProfileByUserId(@Param("userId") Integer userId);

    /** 특정 사진을 대표로 지정 */
    @Modifying
    @Query("UPDATE UserPhoto p SET p.isProfile = true WHERE p.id = :photoId AND p.userId = :userId")
    int setProfile(@Param("userId") Integer userId, @Param("photoId") Integer photoId);
}
