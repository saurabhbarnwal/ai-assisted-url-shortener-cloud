package com.company.urlshortener.mapper;

import com.company.urlshortener.dto.AnalyticsEventResponse;
import com.company.urlshortener.dto.AnalyticsResponse;
import com.company.urlshortener.entity.AnalyticsEntity;
import com.company.urlshortener.entity.UrlEntity;
import java.util.List;
import org.springframework.stereotype.Component;

/** Maps analytics entities to DTOs. */
@Component
public class AnalyticsMapper {

  /** Converts analytics data to a response payload. */
  public AnalyticsResponse toAnalyticsResponse(
      UrlEntity urlEntity, long totalClicks, List<AnalyticsEntity> recentClicks) {
    return new AnalyticsResponse(
        urlEntity.getShortCode(),
        urlEntity.getOriginalUrl(),
        urlEntity.getCreatedAt(),
        totalClicks,
        recentClicks.stream().map(this::toAnalyticsEventResponse).toList());
  }

  /** Converts a click event entity to a response payload. */
  public AnalyticsEventResponse toAnalyticsEventResponse(AnalyticsEntity analyticsEntity) {
    return new AnalyticsEventResponse(
        analyticsEntity.getClickTimestamp(),
        analyticsEntity.getBrowser(),
        analyticsEntity.getDevice(),
        analyticsEntity.getReferrer());
  }
}
