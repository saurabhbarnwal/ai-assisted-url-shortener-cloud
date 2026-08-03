package com.company.urlshortener.exception;

/** Raised when a unique short code cannot be generated. */
public class ShortCodeGenerationException extends RuntimeException {

  /** Creates a short code generation exception. */
  public ShortCodeGenerationException(String message) {
    super(message);
  }
}
