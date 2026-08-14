package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateShortUrlRequest {

    @NotBlank(message = "originalUrl is required")
    @Size(max = 2048)
    private String originalUrl;

    public CreateShortUrlRequest() {}

    public CreateShortUrlRequest(String originalUrl) { this.originalUrl = originalUrl; }
    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
}