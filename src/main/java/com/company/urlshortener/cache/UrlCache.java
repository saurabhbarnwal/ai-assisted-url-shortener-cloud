package com.company.urlshortener.cache;

import com.company.urlshortener.config.ApplicationProperties;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Redis cache for short code to original URL lookups. */
@Component
public class UrlCache {

  private static final Logger LOGGER = LoggerFactory.getLogger(UrlCache.class);

  private final StringRedisTemplate redisTemplate;
  private final Duration cacheTtl;

  /** Creates a URL cache backed by Redis. */
  public UrlCache(StringRedisTemplate redisTemplate, ApplicationProperties applicationProperties) {
    this.redisTemplate = redisTemplate;
    this.cacheTtl = applicationProperties.cacheTtl();
  }

  /** Returns the original URL for a cached short code, when present. */
  public Optional<String> getOriginalUrl(String shortCode) {
    try {
      return Optional.ofNullable(redisTemplate.opsForValue().get(shortCode));
    } catch (RedisConnectionFailureException exception) {
      LOGGER.warn("Redis read failed for short code {}", shortCode);
      return Optional.empty();
    }
  }

  /** Caches a short code to original URL mapping. */
  public void putOriginalUrl(String shortCode, String originalUrl) {
    try {
      redisTemplate.opsForValue().set(shortCode, originalUrl, cacheTtl);
    } catch (RedisConnectionFailureException exception) {
      LOGGER.warn("Redis write failed for short code {}", shortCode);
    }
  }
}
