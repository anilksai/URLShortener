package com.example.demo.dto;

public class CreateShortUrlResponse {
    private String code;
    private String shortUrl;

    public CreateShortUrlResponse() {}
    public CreateShortUrlResponse(String code, String shortUrl) { this.code = code; this.shortUrl = shortUrl; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getShortUrl() { return shortUrl; }
    public void setShortUrl(String shortUrl) { this.shortUrl = shortUrl; }
}