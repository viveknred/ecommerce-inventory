package com.example.ecommerce.dto;

public class MediaUploadResponse {

    private String fileName;
    private String url;
    private String contentType;
    private long sizeBytes;

    public MediaUploadResponse() {
    }

    public MediaUploadResponse(
            String fileName,
            String url,
            String contentType,
            long sizeBytes) {

        this.fileName = fileName;
        this.url = url;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUrl() {
        return url;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }
}
