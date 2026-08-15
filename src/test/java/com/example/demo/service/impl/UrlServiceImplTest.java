package com.example.demo.service.impl;

import com.example.demo.dto.AnalyticsResponse;
import com.example.demo.dto.CreateShortUrlRequest;
import com.example.demo.dto.CreateShortUrlResponse;
import com.example.demo.entity.ShortUrl;
import com.example.demo.exception.InvalidUrlException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.ShortUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.demo.util.ShortCodeService;

public class UrlServiceImplTest {

    private ShortUrlRepository repository;
    private UrlServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(ShortUrlRepository.class);
        ShortCodeService shortCodeService = mock(ShortCodeService.class);
        when(shortCodeService.randomCode(7)).thenReturn("abc1234");
        service = new UrlServiceImpl(repository, shortCodeService);
    }

    @Test
    void createShortUrl_success() {
        when(repository.existsByCode(anyString())).thenReturn(false);
        when(repository.save(any(ShortUrl.class))).thenAnswer(i -> i.getArgument(0));

        CreateShortUrlRequest req = new CreateShortUrlRequest("https://example.com");
        CreateShortUrlResponse resp = service.createShortUrl(req, "http://localhost:8080");

        assertNotNull(resp.getCode());
        assertEquals(7, resp.getCode().length());
        assertTrue(resp.getShortUrl().startsWith("http://localhost:8080/"));
        verify(repository, atLeastOnce()).save(any(ShortUrl.class));
    }

    @Test
    void resolveAndTrack_increments() {
        ShortUrl s = new ShortUrl("abcdefg", "https://example.com");
        when(repository.findByCode("abcdefg")).thenReturn(Optional.of(s));
        when(repository.save(any(ShortUrl.class))).thenAnswer(i -> i.getArgument(0));

        String url = service.resolveAndTrack("abcdefg");
        assertEquals("https://example.com", url);
        assertEquals(1L, s.getRedirectCount());
        verify(repository).save(s);
    }

    @Test
    void resolveAndTrack_notFound() {
        when(repository.findByCode(anyString())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.resolveAndTrack("nope"));
    }

    @Test
    void createShortUrl_rejectsUnsafeUrl() {
        CreateShortUrlRequest req = new CreateShortUrlRequest("javascript:alert(1)");

        assertThrows(InvalidUrlException.class, () -> service.createShortUrl(req, "http://localhost:8080"));
        verify(repository, never()).save(any(ShortUrl.class));
    }

    @Test
    void analytics_returnsData() {
        ShortUrl s = new ShortUrl("code123", "https://x");
        s.setRedirectCount(5);
        when(repository.findByCode("code123")).thenReturn(Optional.of(s));

        AnalyticsResponse a = service.analytics("code123");
        assertEquals("code123", a.getCode());
        assertEquals(5, a.getRedirectCount());
    }
}