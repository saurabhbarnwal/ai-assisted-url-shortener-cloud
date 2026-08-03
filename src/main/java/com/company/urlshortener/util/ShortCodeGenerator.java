package com.company.urlshortener.util;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/** Generates non-sequential URL-safe short codes. */
@Component
public class ShortCodeGenerator {

  private static final char[] ALPHABET =
      "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

  private final SecureRandom secureRandom = new SecureRandom();

  /** Generates a random short code with the requested length. */
  public String generate(int length) {
    StringBuilder builder = new StringBuilder(length);
    for (int index = 0; index < length; index++) {
      builder.append(ALPHABET[secureRandom.nextInt(ALPHABET.length)]);
    }
    return builder.toString();
  }
}
