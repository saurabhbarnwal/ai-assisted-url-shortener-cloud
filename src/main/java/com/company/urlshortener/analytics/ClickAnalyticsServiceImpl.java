package com.company.urlshortener.analytics;

import com.company.urlshortener.dto.AnalyticsResponse;
import com.company.urlshortener.entity.AnalyticsEntity;
import com.company.urlshortener.entity.UrlEntity;
import com.company.urlshortener.exception.UrlNotFoundException;
import com.company.urlshortener.mapper.AnalyticsMapper;
import com.company.urlshortener.repository.AnalyticsRepository;
import com.company.urlshortener.repository.UrlRepository;
import com.company.urlshortener.util.UserAgentParser;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default implementation of click analytics capture and query behavior. */
@Service
public class ClickAnalyticsServiceImpl implements ClickAnalyticsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClickAnalyticsServiceImpl.class);
  private static final int RECENT_CLICK_LIMIT = 25;

  private final AnalyticsRepository analyticsRepository;
  private final UrlRepository urlRepository;
  private final AnalyticsMapper analyticsMapper;
  private final UserAgentParser userAgentParser;

  /** Creates the analytics service. */
  public ClickAnalyticsServiceImpl(
      AnalyticsRepository analyticsRepository,
      UrlRepository urlRepository,
      AnalyticsMapper analyticsMapper,
      UserAgentParser userAgentParser) {
    this.analyticsRepository = analyticsRepository;
    this.urlRepository = urlRepository;
    this.analyticsMapper = analyticsMapper;
    this.userAgentParser = userAgentParser;
  }

  /** Records a click event for a URL. */
  @Override
  @Transactional
  public void recordClick(UrlEntity urlEntity, String userAgent, String referrer) {
    AnalyticsEntity analyticsEntity =
        new AnalyticsEntity(
            urlEntity,
            Instant.now(),
            userAgentParser.browserFrom(userAgent),
            userAgentParser.deviceFrom(userAgent),
            referrer);
    analyticsRepository.save(analyticsEntity);
    LOGGER.info("Recorded click for short code {}", urlEntity.getShortCode());
  }

  /** Returns analytics for a short code. */
  @Override
  @Transactional(readOnly = true)
  public AnalyticsResponse getAnalytics(String shortCode) {
    UrlEntity urlEntity =
        urlRepository.findByShortCode(shortCode).orElseThrow(() -> new UrlNotFoundException(shortCode));
    long totalClicks = analyticsRepository.countByUrl(urlEntity);
    List<AnalyticsEntity> recentClicks =
        analyticsRepository.findByUrlOrderByClickTimestampDesc(
            urlEntity, PageRequest.of(0, RECENT_CLICK_LIMIT));
    return analyticsMapper.toAnalyticsResponse(urlEntity, totalClicks, recentClicks);
  }
}
