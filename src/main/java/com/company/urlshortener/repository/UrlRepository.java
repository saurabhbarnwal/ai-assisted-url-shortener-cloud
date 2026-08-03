package com.company.urlshortener.repository;

import com.company.urlshortener.entity.UrlEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for URL persistence operations. */
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {

  /** Finds a URL by its original value. */
  Optional<UrlEntity> findByOriginalUrl(String originalUrl);

  /** Finds a URL by its short code. */
  Optional<UrlEntity> findByShortCode(String shortCode);

  /** Returns whether a short code already exists. */
  boolean existsByShortCode(String shortCode);
}
