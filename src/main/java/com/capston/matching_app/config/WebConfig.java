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
        String location = Path.of(baseDir).toAbsolutePath().toUri().toString();
        String pattern = publicPrefix.endsWith("/**") ? publicPrefix : publicPrefix + "/**";
        registry.addResourceHandler(pattern).addResourceLocations(location);
    }
}
