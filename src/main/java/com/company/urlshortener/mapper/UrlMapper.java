package com.company.urlshortener.mapper;

import com.company.urlshortener.config.ApplicationProperties;
import com.company.urlshortener.dto.CreateUrlResponse;
import com.company.urlshortener.entity.UrlEntity;
import org.springframework.stereotype.Component;

/** Maps URL entities to DTOs. */
@Component
public class UrlMapper {

  private final ApplicationProperties applicationProperties;

  /** Creates a mapper with application URL settings. */
  public UrlMapper(ApplicationProperties applicationProperties) {
    this.applicationProperties = applicationProperties;
  }

  /** Converts a URL entity to a create response. */
  public CreateUrlResponse toCreateUrlResponse(UrlEntity urlEntity) {
    return new CreateUrlResponse(
        urlEntity.getOriginalUrl(),
        urlEntity.getShortCode(),
        applicationProperties.baseUrl() + "/" + urlEntity.getShortCode(),
        urlEntity.getCreatedAt());
  }
}
