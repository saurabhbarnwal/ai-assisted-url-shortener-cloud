package com.company.urlshortener.dto;

/** Service result that includes whether a URL row was newly created. */
public record CreateUrlResult(CreateUrlResponse response, boolean created) {}
