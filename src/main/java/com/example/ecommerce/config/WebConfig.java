package com.example.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.ecommerce.service.MediaStorageService;

/**
 * Phase 4, Module 4: serves stored uploads as static content.
 *
 * <p>Maps the public URL prefix (default {@code /uploads/**}) onto the local
 * directory the {@link MediaStorageService} writes into, so the URL returned by
 * the upload endpoint is immediately fetchable in a browser. The directory is
 * read back off the service rather than recomputed here, so the write path and
 * the read path cannot drift apart.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MediaProperties mediaProperties;
    private final MediaStorageService mediaStorageService;

    public WebConfig(
            MediaProperties mediaProperties,
            MediaStorageService mediaStorageService) {

        this.mediaProperties = mediaProperties;
        this.mediaStorageService = mediaStorageService;
    }

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry) {

        String basePath =
                normalisePattern(
                        mediaProperties.getPublicBasePath()
                );

        // Spring requires a trailing slash on a file: location, otherwise the
        // last path segment is treated as a filename and nothing resolves.
        String location =
                mediaStorageService
                        .getBaseDirectory()
                        .toUri()
                        .toString();

        if (!location.endsWith("/")) {
            location = location + "/";
        }

        registry
                .addResourceHandler(basePath + "/**")
                .addResourceLocations(location);
    }

    private String normalisePattern(String basePath) {

        String pattern =
                basePath == null || basePath.isBlank()
                        ? "/uploads"
                        : basePath.trim();

        if (!pattern.startsWith("/")) {
            pattern = "/" + pattern;
        }

        while (pattern.endsWith("/")) {
            pattern = pattern.substring(0, pattern.length() - 1);
        }

        return pattern;
    }
}
