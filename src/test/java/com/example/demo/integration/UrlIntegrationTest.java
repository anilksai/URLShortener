package com.example.demo.integration;

import com.example.demo.dto.CreateShortUrlRequest;
import com.example.demo.entity.ShortUrl;
import com.example.demo.repository.ShortUrlRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UrlIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ShortUrlRepository repository;

    @Test
    void createAndRedirectAndAnalytics() {
        String base = "http://localhost:" + port;
        CreateShortUrlRequest req = new CreateShortUrlRequest("https://example.org");
        ResponseEntity<String> r = restTemplate.postForEntity(base + "/api/shorten", req, String.class);
        Assertions.assertEquals(HttpStatus.OK, r.getStatusCode());
        Assertions.assertTrue(r.getBody().contains("code"));

        // parse code from body crudely
        String body = r.getBody();
        String code = body.replaceAll("(?s).*\"code\"\\s*:\\s*\"([^\"]+)\".*", "$1");
        Assertions.assertNotNull(code);

        // simulate browser following redirect
        ResponseEntity<String> redirect = restTemplate.getForEntity(base + "/" + code, String.class);
        Assertions.assertTrue(redirect.getStatusCode().is3xxRedirection());
        Assertions.assertEquals("https://example.org", redirect.getHeaders().getLocation().toString());

        // analytics
        ResponseEntity<String> analytics = restTemplate.getForEntity(base + "/api/analytics/" + code, String.class);
        Assertions.assertEquals(HttpStatus.OK, analytics.getStatusCode());
        Assertions.assertTrue(analytics.getBody().contains("redirectCount"));

        // verify repository contains entry
        ShortUrl s = repository.findByCode(code).orElseThrow();
        Assertions.assertEquals("https://example.org", s.getOriginalUrl());
    }
}