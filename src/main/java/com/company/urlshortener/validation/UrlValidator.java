package com.company.urlshortener.validation;

import com.company.urlshortener.exception.InvalidUrlException;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.stereotype.Component;

/** Validates user-submitted original URLs. */
@Component
public class UrlValidator {

  /** Validates and normalizes an original URL string. */
  public String validateOriginalUrl(String originalUrl) {
    String trimmedUrl = originalUrl == null ? "" : originalUrl.trim();
    if (trimmedUrl.isEmpty()) {
      throw new InvalidUrlException("originalUrl is required");
    }

    try {
      URI uri = new URI(trimmedUrl);
      String scheme = uri.getScheme();
      if (scheme == null
          || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
        throw new InvalidUrlException("Only http and https URLs are supported");
      }
      if (uri.getHost() == null || uri.getHost().isBlank()) {
        throw new InvalidUrlException("URL host is required");
      }
      if (uri.getUserInfo() != null) {
        throw new InvalidUrlException("URLs containing credentials are not allowed");
      }
      return uri.normalize().toString();
    } catch (URISyntaxException exception) {
      throw new InvalidUrlException("originalUrl must be a valid URL");
    }
  }
}
