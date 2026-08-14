package com.example.demo.service;

import com.example.demo.dto.AnalyticsResponse;
import com.example.demo.dto.CreateShortUrlRequest;
import com.example.demo.dto.CreateShortUrlResponse;

public interface UrlService {
    CreateShortUrlResponse createShortUrl(CreateShortUrlRequest request, String baseUrl);
    String resolveAndTrack(String code);
    AnalyticsResponse analytics(String code);
}