package com.example.demo.service.impl;

import com.example.demo.dto.AnalyticsResponse;
import com.example.demo.dto.CreateShortUrlRequest;
import com.example.demo.dto.CreateShortUrlResponse;
import com.example.demo.entity.ShortUrl;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.ShortUrlRepository;
import com.example.demo.service.UrlService;
import com.example.demo.util.ShortCodeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.util.UrlSafetyGuard;

@Service
public class UrlServiceImpl implements UrlService {

    private final ShortUrlRepository repository;
    private final ShortCodeService shortCodeService;

    public UrlServiceImpl(ShortUrlRepository repository, ShortCodeService shortCodeService) {
        this.repository = repository;
        this.shortCodeService = shortCodeService;
    }

    @Override
    @Transactional
    public CreateShortUrlResponse createShortUrl(CreateShortUrlRequest request, String baseUrl) {
        String safeUrl = UrlSafetyGuard.normalizeAndValidate(request.getOriginalUrl());

        String code;
        // generate unique code (bounded attempts)
        int attempts = 0;
        do {
            code = shortCodeService.randomCode(7);
            attempts++;
            if (attempts > 10) throw new IllegalStateException("Unable to generate unique short code");
        } while (repository.existsByCode(code));

        ShortUrl entity = new ShortUrl(code, safeUrl);
        repository.save(entity);

        String shortUrl = baseUrl != null ? String.format("%s/%s", baseUrl.replaceAll("/+$", ""), code) : code;
        return new CreateShortUrlResponse(code, shortUrl);
    }

    @Override
    @Transactional
    public String resolveAndTrack(String code) {
        ShortUrl su = repository.findByCode(code).orElseThrow(() -> new NotFoundException("Code not found"));
        su.incrementRedirectCount();
        repository.save(su);
        return su.getOriginalUrl();
    }

    @Override
    public AnalyticsResponse analytics(String code) {
        ShortUrl su = repository.findByCode(code).orElseThrow(() -> new NotFoundException("Code not found"));
        return new AnalyticsResponse(su.getCode(), su.getOriginalUrl(), su.getRedirectCount(), su.getCreatedAt());
    }
}