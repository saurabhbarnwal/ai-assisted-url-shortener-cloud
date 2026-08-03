package com.company.urlshortener.exception;

/** Raised when a short code does not resolve to a URL. */
public class UrlNotFoundException extends RuntimeException {

  /** Creates a URL not found exception. */
  public UrlNotFoundException(String shortCode) {
    super("Short code not found: " + shortCode);
  }
}
