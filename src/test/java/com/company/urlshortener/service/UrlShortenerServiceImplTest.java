package com.company.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.urlshortener.analytics.ClickAnalyticsService;
import com.company.urlshortener.cache.UrlCache;
import com.company.urlshortener.config.ApplicationProperties;
import com.company.urlshortener.dto.CreateUrlRequest;
import com.company.urlshortener.dto.CreateUrlResult;
import com.company.urlshortener.entity.UrlEntity;
import com.company.urlshortener.exception.InvalidUrlException;
import com.company.urlshortener.exception.UrlNotFoundException;
import com.company.urlshortener.mapper.UrlMapper;
import com.company.urlshortener.repository.UrlRepository;
import com.company.urlshortener.service.impl.UrlShortenerServiceImpl;
import com.company.urlshortener.util.ShortCodeGenerator;
import com.company.urlshortener.validation.UrlValidator;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for URL shortening business rules. */
@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceImplTest {

  @Mock private UrlRepository urlRepository;
  @Mock private UrlCache urlCache;
  @Mock private ShortCodeGenerator shortCodeGenerator;
  @Mock private ClickAnalyticsService clickAnalyticsService;

  private UrlShortenerServiceImpl service;

  /** Creates the service under test. */
  @BeforeEach
  void setUp() {
    ApplicationProperties properties =
        new ApplicationProperties("http://localhost:8080", 7, 3, Duration.ofHours(24));
    UrlMapper urlMapper = new UrlMapper(properties);
    service =
        new UrlShortenerServiceImpl(
            urlRepository,
            urlCache,
            urlMapper,
            new UrlValidator(),
            shortCodeGenerator,
            clickAnalyticsService,
            properties);
  }

  /** Creates a new shortened URL when the original URL is not already stored. */
  @Test
  void createShortUrlPersistsNewUrl() {
    when(urlRepository.findByOriginalUrl("https://example.com/a")).thenReturn(Optional.empty());
    when(shortCodeGenerator.generate(7)).thenReturn("AbC123x");
    when(urlRepository.existsByShortCode("AbC123x")).thenReturn(false);
    when(urlRepository.save(any(UrlEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    CreateUrlResult result =
        service.createShortUrl(new CreateUrlRequest("https://example.com/a"));

    assertThat(result.created()).isTrue();
    assertThat(result.response().shortCode()).isEqualTo("AbC123x");
    assertThat(result.response().shortUrl()).isEqualTo("http://localhost:8080/AbC123x");
    verify(urlCache).putOriginalUrl("AbC123x", "https://example.com/a");
  }

  /** Returns the existing short URL for duplicate original URLs. */
  @Test
  void createShortUrlReturnsExistingUrlForDuplicateOriginalUrl() {
    UrlEntity existingUrl = new UrlEntity("https://example.com/a", "Exists1");
    when(urlRepository.findByOriginalUrl("https://example.com/a")).thenReturn(Optional.of(existingUrl));

    CreateUrlResult result =
        service.createShortUrl(new CreateUrlRequest("https://example.com/a"));

    assertThat(result.created()).isFalse();
    assertThat(result.response().shortCode()).isEqualTo("Exists1");
    verify(urlRepository, never()).save(any());
  }

  /** Rejects unsupported URL schemes. */
  @Test
  void createShortUrlRejectsInvalidUrl() {
    assertThatThrownBy(() -> service.createShortUrl(new CreateUrlRequest("ftp://example.com/a")))
        .isInstanceOf(InvalidUrlException.class)
        .hasMessageContaining("Only http and https");
  }

  /** Resolves a cached URL and still records analytics. */
  @Test
  void resolveOriginalUrlUsesCacheAndRecordsAnalytics() {
    UrlEntity urlEntity = new UrlEntity("https://example.com/a", "AbC123x");
    when(urlCache.getOriginalUrl("AbC123x")).thenReturn(Optional.of("https://example.com/a"));
    when(urlRepository.findByShortCode("AbC123x")).thenReturn(Optional.of(urlEntity));

    String originalUrl = service.resolveOriginalUrl("AbC123x", "Mozilla/5.0 Chrome/120", "ref");

    assertThat(originalUrl).isEqualTo("https://example.com/a");
    verify(urlCache, never()).putOriginalUrl(eq("AbC123x"), any());
    verify(clickAnalyticsService).recordClick(urlEntity, "Mozilla/5.0 Chrome/120", "ref");
  }

  /** Throws not found when a short code does not exist. */
  @Test
  void resolveOriginalUrlThrowsWhenShortCodeMissing() {
    when(urlCache.getOriginalUrl("missing")).thenReturn(Optional.empty());
    when(urlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.resolveOriginalUrl("missing", null, null))
        .isInstanceOf(UrlNotFoundException.class);
  }
}
