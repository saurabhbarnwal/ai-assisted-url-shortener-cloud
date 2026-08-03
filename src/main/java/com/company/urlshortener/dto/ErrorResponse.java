package com.company.urlshortener.dto;

import java.time.Instant;
import java.util.List;

/** Standard API error response. */
public record ErrorResponse(
    Instant timestamp, int status, String error, String message, List<String> details, String path) {}
