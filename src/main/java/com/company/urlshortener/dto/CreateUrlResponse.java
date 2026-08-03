package com.company.urlshortener.dto;

import java.time.Instant;

/** Response payload returned after creating or detecting a shortened URL. */
public record CreateUrlResponse(
    String originalUrl, String shortCode, String shortUrl, Instant createdAt) {}
