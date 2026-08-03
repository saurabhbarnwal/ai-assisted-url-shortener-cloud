package com.company.urlshortener.dto;

import java.time.Instant;

/** Response payload for a single analytics event. */
public record AnalyticsEventResponse(
    Instant clickTimestamp, String browser, String device, String referrer) {}
