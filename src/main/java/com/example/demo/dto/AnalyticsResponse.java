package com.example.demo.dto;

import java.time.Instant;

public class AnalyticsResponse {
    private String code;
    private String originalUrl;
    private long redirectCount;
    private Instant createdAt;

    public AnalyticsResponse() {}

    public AnalyticsResponse(String code, String originalUrl, long redirectCount, Instant createdAt) {
        this.code = code; this.originalUrl = originalUrl; this.redirectCount = redirectCount; this.createdAt = createdAt;
    }

    public String getCode() { return code; }
    public String getOriginalUrl() { return originalUrl; }
    public long getRedirectCount() { return redirectCount; }
    public Instant getCreatedAt() { return createdAt; }
}