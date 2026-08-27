package com.example.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the {@code app.media.*} keys from application.properties.
 *
 * <p>Keeping the upload limits in configuration rather than constants means the
 * 2 MB ceiling can be tuned per environment without a recompile, and the same
 * value is shared by the service-level check and the Swagger documentation.
 */
@ConfigurationProperties(prefix = "app.media")
public class MediaProperties {

    /**
     * Directory on local disk where uploads are written. Relative paths resolve
     * against the process working directory.
     */
    private String uploadDir = "uploads";

    /**
     * URL prefix the stored files are served under.
     */
    private String publicBasePath = "/uploads";

    /**
     * Hard ceiling per file, in bytes. Defaults to 2 MB.
     */
    private long maxFileSizeBytes = 2 * 1024 * 1024;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getPublicBasePath() {
        return publicBasePath;
    }

    public void setPublicBasePath(String publicBasePath) {
        this.publicBasePath = publicBasePath;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }
}
