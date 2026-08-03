package com.company.urlshortener.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for URL generation and caching. */
@ConfigurationProperties(prefix = "app")
public record ApplicationProperties(
    String baseUrl, int shortCodeLength, int shortCodeMaxAttempts, Duration cacheTtl) {}
