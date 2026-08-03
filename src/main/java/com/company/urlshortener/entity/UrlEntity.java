package com.company.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** Represents a shortened URL persisted in PostgreSQL. */
@Entity
@Table(name = "urls")
@EntityListeners(AuditingEntityListener.class)
public class UrlEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "original_url", nullable = false, unique = true, length = 2048)
  private String originalUrl;

  @Column(name = "short_code", nullable = false, unique = true, length = 32)
  private String shortCode;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected UrlEntity() {}

  /** Creates a URL entity with an original URL and generated short code. */
  public UrlEntity(String originalUrl, String shortCode) {
    this.originalUrl = originalUrl;
    this.shortCode = shortCode;
  }

  /** Returns the database identifier. */
  public Long getId() {
    return id;
  }

  /** Returns the original URL. */
  public String getOriginalUrl() {
    return originalUrl;
  }

  /** Returns the short code. */
  public String getShortCode() {
    return shortCode;
  }

  /** Returns the creation timestamp. */
  public Instant getCreatedAt() {
    return createdAt;
  }
}
