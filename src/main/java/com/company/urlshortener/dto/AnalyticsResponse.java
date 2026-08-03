package com.company.urlshortener.dto;

import java.time.Instant;
import java.util.List;

/** Response payload containing click analytics for a short code. */
public record AnalyticsResponse(
    String shortCode,
    String originalUrl,
    Instant createdAt,
    long totalClicks,
    List<AnalyticsEventResponse> recentClicks) {}
