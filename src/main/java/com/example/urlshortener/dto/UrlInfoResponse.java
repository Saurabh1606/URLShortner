package com.example.urlshortener.dto;


public class UrlInfoResponse {
    private String shortCode;
    private String originalUrl;
    private String status;

    // Constructors
    public UrlInfoResponse(String shortCode, String originalUrl, String status) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.status = status;
    }

    // Getters and setters
    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

