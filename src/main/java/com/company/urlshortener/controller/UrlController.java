package com.company.urlshortener.controller;

import com.company.urlshortener.analytics.ClickAnalyticsService;
import com.company.urlshortener.dto.AnalyticsResponse;
import com.company.urlshortener.dto.CreateUrlRequest;
import com.company.urlshortener.dto.CreateUrlResponse;
import com.company.urlshortener.dto.CreateUrlResult;
import com.company.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for URL creation, redirects, and analytics. */
@RestController
@RequestMapping
public class UrlController {

  private final UrlShortenerService urlShortenerService;
  private final ClickAnalyticsService clickAnalyticsService;

  /** Creates the URL controller. */
  public UrlController(
      UrlShortenerService urlShortenerService, ClickAnalyticsService clickAnalyticsService) {
    this.urlShortenerService = urlShortenerService;
    this.clickAnalyticsService = clickAnalyticsService;
  }

  /** Creates a shortened URL. */
  @PostMapping("/api/v1/urls")
  public ResponseEntity<CreateUrlResponse> createShortUrl(
      @Valid @RequestBody CreateUrlRequest createUrlRequest) {
    CreateUrlResult createUrlResult = urlShortenerService.createShortUrl(createUrlRequest);
    HttpStatus status = createUrlResult.created() ? HttpStatus.CREATED : HttpStatus.OK;
    return ResponseEntity.status(status).body(createUrlResult.response());
  }

  /** Redirects a short code to its original URL. */
  @GetMapping("/{shortCode:[A-Za-z0-9]{4,32}}")
  public ResponseEntity<Void> redirect(
      @PathVariable String shortCode, HttpServletRequest httpServletRequest) {
    String originalUrl =
        urlShortenerService.resolveOriginalUrl(
            shortCode,
            httpServletRequest.getHeader(HttpHeaders.USER_AGENT),
            httpServletRequest.getHeader(HttpHeaders.REFERER));
    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(originalUrl)).build();
  }

  /** Returns analytics for a short code. */
  @GetMapping("/api/v1/analytics/{shortCode}")
  public ResponseEntity<AnalyticsResponse> getAnalytics(@PathVariable String shortCode) {
    return ResponseEntity.ok(clickAnalyticsService.getAnalytics(shortCode));
  }
}
