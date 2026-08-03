package com.company.urlshortener.service;

import com.company.urlshortener.dto.CreateUrlRequest;
import com.company.urlshortener.dto.CreateUrlResult;

/** Coordinates URL creation and resolution use cases. */
public interface UrlShortenerService {

  /** Creates or returns an existing shortened URL. */
  CreateUrlResult createShortUrl(CreateUrlRequest createUrlRequest);

  /** Resolves a short code to its original URL and records analytics. */
  String resolveOriginalUrl(String shortCode, String userAgent, String referrer);
}
