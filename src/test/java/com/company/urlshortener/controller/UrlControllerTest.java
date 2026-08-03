package com.company.urlshortener.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.urlshortener.analytics.ClickAnalyticsService;
import com.company.urlshortener.dto.AnalyticsResponse;
import com.company.urlshortener.dto.CreateUrlRequest;
import com.company.urlshortener.dto.CreateUrlResponse;
import com.company.urlshortener.dto.CreateUrlResult;
import com.company.urlshortener.service.UrlShortenerService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** MVC tests for URL controller endpoints. */
@WebMvcTest(UrlController.class)
class UrlControllerTest {

  private final MockMvc mockMvc;

  @MockBean private UrlShortenerService urlShortenerService;
  @MockBean private ClickAnalyticsService clickAnalyticsService;

  @Autowired
  UrlControllerTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  /** Verifies first-time creation returns 201. */
  @Test
  void createShortUrlReturnsCreated() throws Exception {
    when(urlShortenerService.createShortUrl(any(CreateUrlRequest.class)))
        .thenReturn(
            new CreateUrlResult(
                new CreateUrlResponse(
                    "https://example.com", "AbC123x", "http://localhost:8080/AbC123x", Instant.now()),
                true));

    mockMvc
        .perform(
            post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"originalUrl\":\"https://example.com\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.shortCode").value("AbC123x"));
  }

  /** Verifies duplicate creation returns 200. */
  @Test
  void createShortUrlReturnsOkForDuplicateUrl() throws Exception {
    when(urlShortenerService.createShortUrl(any(CreateUrlRequest.class)))
        .thenReturn(
            new CreateUrlResult(
                new CreateUrlResponse(
                    "https://example.com", "AbC123x", "http://localhost:8080/AbC123x", Instant.now()),
                false));

    mockMvc
        .perform(
            post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"originalUrl\":\"https://example.com\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/AbC123x"));
  }

  /** Verifies redirect responses include a Location header. */
  @Test
  void redirectReturnsFound() throws Exception {
    when(urlShortenerService.resolveOriginalUrl(eq("AbC123x"), any(), any()))
        .thenReturn("https://example.com");

    mockMvc
        .perform(get("/AbC123x").header(HttpHeaders.USER_AGENT, "Mozilla/5.0 Chrome/120"))
        .andExpect(status().isFound())
        .andExpect(header().string(HttpHeaders.LOCATION, "https://example.com"));
  }

  /** Verifies analytics endpoint returns analytics payload. */
  @Test
  void getAnalyticsReturnsPayload() throws Exception {
    when(clickAnalyticsService.getAnalytics("AbC123x"))
        .thenReturn(
            new AnalyticsResponse(
                "AbC123x", "https://example.com", Instant.now(), 2L, List.of()));

    mockMvc
        .perform(get("/api/v1/analytics/AbC123x"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalClicks").value(2));
  }

  /** Verifies request body validation is applied. */
  @Test
  void createShortUrlRejectsBlankOriginalUrl() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"originalUrl\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Request validation failed"));
  }
}
