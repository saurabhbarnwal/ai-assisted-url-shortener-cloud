package com.company.urlshortener.exception;

/** Raised when a submitted original URL is invalid. */
public class InvalidUrlException extends RuntimeException {

  /** Creates an invalid URL exception. */
  public InvalidUrlException(String message) {
    super(message);
  }
}
