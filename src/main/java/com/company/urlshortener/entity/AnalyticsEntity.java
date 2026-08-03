package com.company.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/** Represents a single redirect click event. */
@Entity
@Table(name = "analytics")
public class AnalyticsEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "url_id", nullable = false)
  private UrlEntity url;

  @Column(name = "click_timestamp", nullable = false)
  private Instant clickTimestamp;

  @Column(name = "browser", nullable = false, length = 128)
  private String browser;

  @Column(name = "device", nullable = false, length = 128)
  private String device;

  @Column(name = "referrer", length = 2048)
  private String referrer;

  protected AnalyticsEntity() {}

  /** Creates an analytics click event for a URL. */
  public AnalyticsEntity(
      UrlEntity url, Instant clickTimestamp, String browser, String device, String referrer) {
    this.url = url;
    this.clickTimestamp = clickTimestamp;
    this.browser = browser;
    this.device = device;
    this.referrer = referrer;
  }

  /** Returns the analytics identifier. */
  public Long getId() {
    return id;
  }

  /** Returns the URL associated with the click. */
  public UrlEntity getUrl() {
    return url;
  }

  /** Returns the click timestamp. */
  public Instant getClickTimestamp() {
    return clickTimestamp;
  }

  /** Returns the detected browser. */
  public String getBrowser() {
    return browser;
  }

  /** Returns the detected device. */
  public String getDevice() {
    return device;
  }

  /** Returns the HTTP referrer, when available. */
  public String getReferrer() {
    return referrer;
  }
}
