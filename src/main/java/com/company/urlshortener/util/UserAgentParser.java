package com.company.urlshortener.util;

import java.util.Locale;
import org.springframework.stereotype.Component;

/** Extracts coarse browser and device attributes from user agent strings. */
@Component
public class UserAgentParser {

  /** Returns a browser name from a user agent string. */
  public String browserFrom(String userAgent) {
    String normalizedUserAgent = normalize(userAgent);
    if (normalizedUserAgent.contains("edg/")) {
      return "Edge";
    }
    if (normalizedUserAgent.contains("chrome/") && !normalizedUserAgent.contains("chromium")) {
      return "Chrome";
    }
    if (normalizedUserAgent.contains("firefox/")) {
      return "Firefox";
    }
    if (normalizedUserAgent.contains("safari/") && !normalizedUserAgent.contains("chrome/")) {
      return "Safari";
    }
    return "Unknown";
  }

  /** Returns a coarse device type from a user agent string. */
  public String deviceFrom(String userAgent) {
    String normalizedUserAgent = normalize(userAgent);
    if (normalizedUserAgent.contains("mobile")
        || normalizedUserAgent.contains("android")
        || normalizedUserAgent.contains("iphone")) {
      return "Mobile";
    }
    if (normalizedUserAgent.contains("ipad") || normalizedUserAgent.contains("tablet")) {
      return "Tablet";
    }
    if (normalizedUserAgent.isBlank()) {
      return "Unknown";
    }
    return "Desktop";
  }

  private String normalize(String value) {
    return value == null ? "" : value.toLowerCase(Locale.ROOT);
  }
}
