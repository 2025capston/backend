package com.capston.matching_app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.base-dir}")
    private String baseDir;

    @Value("${app.upload.public-prefix:/static/user-photos}")
    private String publicPrefix;

    public String saveUserPhoto(Integer userId, MultipartFile file) {
        try {
            String ext = guessExtension(file.getOriginalFilename());
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String name = ts + "_" + UUID.randomUUID().toString().substring(0,8) + ext;

            Path userDir = Path.of(baseDir, String.valueOf(userId)).toAbsolutePath().normalize();
            Files.createDirectories(userDir);

            Path target = userDir.resolve(name);
            Files.copy(file.getInputStream(), target);

            String pubPrefix = publicPrefix.endsWith("/") ? publicPrefix.substring(0, publicPrefix.length()-1) : publicPrefix;
            return pubPrefix + "/" + userId + "/" + name;
        } catch (Exception e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    private String guessExtension(String original) {
        if (original == null) return ".jpg";
        String lower = original.toLowerCase();
        if (lower.endsWith(".png"))  return ".png";
        if (lower.endsWith(".jpeg")) return ".jpeg";
        if (lower.endsWith(".jpg"))  return ".jpg";
        if (lower.endsWith(".webp")) return ".webp";
        return ".jpg";
    }
}
