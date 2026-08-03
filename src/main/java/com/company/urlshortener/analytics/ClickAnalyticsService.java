package com.company.urlshortener.analytics;

import com.company.urlshortener.dto.AnalyticsResponse;
import com.company.urlshortener.entity.UrlEntity;

/** Captures and queries click analytics. */
public interface ClickAnalyticsService {

  /** Records a click event for a URL. */
  void recordClick(UrlEntity urlEntity, String userAgent, String referrer);

  /** Returns analytics for a short code. */
  AnalyticsResponse getAnalytics(String shortCode);
}
