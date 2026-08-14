package com.example.demo.controller;

import com.example.demo.dto.AnalyticsResponse;
import com.example.demo.dto.CreateShortUrlRequest;
import com.example.demo.dto.CreateShortUrlResponse;
import com.example.demo.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) { this.urlService = urlService; }

    @GetMapping(path = {"/health", "/api/health"})
    public @ResponseBody ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("service", "URLShortener");
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/api/shorten")
    public @ResponseBody ResponseEntity<CreateShortUrlResponse> create(@Valid @RequestBody CreateShortUrlRequest request, HttpServletRequest http) {
        String base = http.getRequestURL().toString().replaceAll(http.getRequestURI() + "$", "");
        CreateShortUrlResponse resp = urlService.createShortUrl(request, base);
        return ResponseEntity.ok(resp);
    }

    @GetMapping(path = "/{code}")
    public ResponseEntity<Void> redirect(@PathVariable("code") String code) {
        String target = urlService.resolveAndTrack(code);
        return ResponseEntity.status(302).header(HttpHeaders.LOCATION, target).build();
    }

    @GetMapping(path = "/api/analytics/{code}")
    public @ResponseBody ResponseEntity<AnalyticsResponse> analytics(@PathVariable("code") String code) {
        AnalyticsResponse resp = urlService.analytics(code);
        return ResponseEntity.ok(resp);
    }
}