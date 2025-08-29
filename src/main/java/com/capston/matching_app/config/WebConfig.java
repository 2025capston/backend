package com.capston.matching_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.base-dir}")
    private String baseDir;

    @Value("${app.upload.public-prefix:/static/user-photos}")
    private String publicPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 디스크 경로 → file: URI 로 변환
        String location = Path.of(baseDir).toAbsolutePath().toUri().toString(); // ex) file:/var/app/uploads/
        // 공개 핸들러 패턴
        String pattern = publicPrefix.endsWith("/**") ? publicPrefix : publicPrefix + "/**"; // ex) /static/user-photos/**
        registry.addResourceHandler(pattern).addResourceLocations(location);
    }
}

