package com.company.urlshortener.service.impl;

import com.company.urlshortener.analytics.ClickAnalyticsService;
import com.company.urlshortener.cache.UrlCache;
import com.company.urlshortener.config.ApplicationProperties;
import com.company.urlshortener.dto.CreateUrlRequest;
import com.company.urlshortener.dto.CreateUrlResult;
import com.company.urlshortener.entity.UrlEntity;
import com.company.urlshortener.exception.ShortCodeGenerationException;
import com.company.urlshortener.exception.UrlNotFoundException;
import com.company.urlshortener.mapper.UrlMapper;
import com.company.urlshortener.repository.UrlRepository;
import com.company.urlshortener.service.UrlShortenerService;
import com.company.urlshortener.util.ShortCodeGenerator;
import com.company.urlshortener.validation.UrlValidator;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default implementation of URL shortening business rules. */
@Service
public class UrlShortenerServiceImpl implements UrlShortenerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UrlShortenerServiceImpl.class);

  private final UrlRepository urlRepository;
  private final UrlCache urlCache;
  private final UrlMapper urlMapper;
  private final UrlValidator urlValidator;
  private final ShortCodeGenerator shortCodeGenerator;
  private final ClickAnalyticsService clickAnalyticsService;
  private final ApplicationProperties applicationProperties;

  /** Creates the URL shortener service. */
  public UrlShortenerServiceImpl(
      UrlRepository urlRepository,
      UrlCache urlCache,
      UrlMapper urlMapper,
      UrlValidator urlValidator,
      ShortCodeGenerator shortCodeGenerator,
      ClickAnalyticsService clickAnalyticsService,
      ApplicationProperties applicationProperties) {
    this.urlRepository = urlRepository;
    this.urlCache = urlCache;
    this.urlMapper = urlMapper;
    this.urlValidator = urlValidator;
    this.shortCodeGenerator = shortCodeGenerator;
    this.clickAnalyticsService = clickAnalyticsService;
    this.applicationProperties = applicationProperties;
  }

  /** Creates or returns an existing shortened URL. */
  @Override
  public CreateUrlResult createShortUrl(CreateUrlRequest createUrlRequest) {
    String originalUrl = urlValidator.validateOriginalUrl(createUrlRequest.originalUrl());
    return urlRepository
        .findByOriginalUrl(originalUrl)
        .map(urlEntity -> new CreateUrlResult(urlMapper.toCreateUrlResponse(urlEntity), false))
        .orElseGet(() -> persistNewUrl(originalUrl));
  }

  /** Resolves a short code to its original URL and records analytics. */
  @Override
  @Transactional
  public String resolveOriginalUrl(String shortCode, String userAgent, String referrer) {
    String cachedOriginalUrl = urlCache.getOriginalUrl(shortCode).orElse(null);
    UrlEntity urlEntity =
        urlRepository.findByShortCode(shortCode).orElseThrow(() -> new UrlNotFoundException(shortCode));

    String originalUrl = cachedOriginalUrl == null ? urlEntity.getOriginalUrl() : cachedOriginalUrl;
    if (cachedOriginalUrl == null) {
      urlCache.putOriginalUrl(shortCode, originalUrl);
    }

    clickAnalyticsService.recordClick(urlEntity, userAgent, referrer);
    return originalUrl;
  }

  private CreateUrlResult persistNewUrl(String originalUrl) {
    for (int attempt = 0; attempt < applicationProperties.shortCodeMaxAttempts(); attempt++) {
      String shortCode = shortCodeGenerator.generate(applicationProperties.shortCodeLength());
      if (urlRepository.existsByShortCode(shortCode)) {
        continue;
      }

      try {
        UrlEntity urlEntity = urlRepository.save(new UrlEntity(originalUrl, shortCode));
        urlRepository.flush();
        urlCache.putOriginalUrl(shortCode, originalUrl);
        LOGGER.info("Created short code {}", shortCode);
        return new CreateUrlResult(urlMapper.toCreateUrlResponse(urlEntity), true);
      } catch (DataIntegrityViolationException exception) {
        Optional<UrlEntity> duplicateOriginalUrl = urlRepository.findByOriginalUrl(originalUrl);
        if (duplicateOriginalUrl.isPresent()) {
          return new CreateUrlResult(
              urlMapper.toCreateUrlResponse(duplicateOriginalUrl.get()), false);
        }
        LOGGER.warn("Short code collision detected for {}", shortCode);
      }
    }
    throw new ShortCodeGenerationException("Unable to generate a unique short code");
  }
}
