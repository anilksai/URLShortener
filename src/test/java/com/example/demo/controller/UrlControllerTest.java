package com.example.demo.controller;

import com.example.demo.dto.CreateShortUrlRequest;
import com.example.demo.dto.CreateShortUrlResponse;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.service.UrlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UrlControllerTest {

    private MockMvc mockMvc;
    private UrlService service;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = mock(UrlService.class);
        UrlController controller = new UrlController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void health_endpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("URLShortener"));
    }

    @Test
    void create_endpoint() throws Exception {
        when(service.createShortUrl(any(CreateShortUrlRequest.class), anyString()))
                .thenReturn(new CreateShortUrlResponse("abc1234", "http://localhost/abc1234"));

        CreateShortUrlRequest req = new CreateShortUrlRequest("https://example.com");
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("abc1234"))
                .andExpect(jsonPath("$.shortUrl").exists());

        verify(service, times(1)).createShortUrl(any(CreateShortUrlRequest.class), anyString());
    }
}